/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.dev;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.WatchableBeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.context.reload.ResourceKind;
import io.micronaut.context.watch.BeanDefinitionChange;
import io.micronaut.context.watch.BeanDefinitionWatcher;
import io.micronaut.context.watch.ClassChangeWatcher;
import io.micronaut.context.watch.ExecutableMethodChange;
import io.micronaut.context.watch.ExecutableMethodWatcher;
import io.micronaut.context.watch.ResourceChange;
import io.micronaut.context.watch.ResourceWatcher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.order.Ordered;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.embedding.InMemoryEmbeddingStoreConfig;
import io.micronaut.langchain4j.interceptor.AiServiceInterceptor;
import io.micronaut.langchain4j.tools.ToolRegistry;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;

/**
 * Keeps the AI services in step with the application in development mode, without a restart. It exists only in
 * development mode, so nothing of it is on the path of an AI service call.
 *
 * <p>An AI service is built once, on its first call, from the tools, models, chat memory and retrievers of its
 * generation, and kept by the {@link AiServiceInterceptor}; the {@link ToolRegistry} keeps the beans that declare
 * tools. A restart builds them again in the next context, and a change of a method body applied in place runs the
 * new body in the tools already built. What changes in place otherwise is followed here:</p>
 *
 * <ul>
 *     <li>A {@link Tool} method added, removed or replaced, or an {@link AiService} definition added, removed or
 *     replaced, recreates the tool registry and the interceptor: the AI services are built again on their next call,
 *     with the current tools. The beans that received the interceptor, the AI services themselves and the beans that
 *     received them, are created again on top of it.</li>
 *     <li>A class change applied in place that retires a class loader does the same, when there are tools or AI
 *     services: what was built holds instances of the retired classes.</li>
 *     <li>A change of the file an in-memory embedding store is read from
 *     ({@code langchain4j.in-memory.embedding-stores.*.path}) recreates that store, and the AI services, which hold the
 *     retrievers built on it.</li>
 * </ul>
 *
 * <p>As a class change retires a class loader, in place or with a restart, the methods of the retired AI services are
 * removed from the set LangChain4j keeps, in a static field, of the AI service methods it validated on a call, which
 * would otherwise keep every retired generation reachable.</p>
 *
 * <p>Prompt templates that an AI service reads from a resource ({@code fromResource}) are read by LangChain4j on each
 * call, so they need nothing here. Each bean is recreated through {@link WatchableBeanContext#recreate(Object)}; a
 * context that does not track bean dependencies recreates nothing. The watches run after those of other modules. This
 * bean holds the context only.</p>
 *
 * @author graemerocher
 * @since 2.4.0
 */
@Internal
@Context
@DevelopmentActive
final class DevelopmentAiServicesReloader {

    private static final Logger LOG = LoggerFactory.getLogger(DevelopmentAiServicesReloader.class);
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";

    private final BeanContext beanContext;
    private volatile boolean anyTools;
    private volatile boolean anyServices;

    /**
     * @param beanContext The context, watched when it can be
     */
    DevelopmentAiServicesReloader(BeanContext beanContext) {
        this.beanContext = beanContext;
        if (beanContext instanceof WatchableBeanContext watchable) {
            watchable.methods(Tool.class).watch(new ToolsWatcher());
            watchable.definitions().stereotype(AiService.class).watch(new ServicesWatcher());
            watchable.classChanges().watch(new ClassWatcher());
            watchable.resources(ResourceKind.CONFIG).watch(new StoreFilesWatcher());
        }
    }

    /**
     * Builds the AI services again on their next call: the tool registry is recreated from the current tools, and the
     * interceptor, which keeps the services it built, with the beans that received it.
     *
     * @param reason Why, for the log
     */
    private void rebuildServices(String reason) {
        if (!(beanContext instanceof WatchableBeanContext context)) {
            return;
        }
        boolean tools = context.recreate(ToolRegistry.class, null);
        boolean interceptor = context.recreate(AiServiceInterceptor.class, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rebuilding the AI services, since {}: tool registry recreated {}, interceptor recreated {}", reason, tools, interceptor);
        }
    }

    private void onStoreFiles(ResourceChange change) {
        if (change.initial() || !(beanContext instanceof WatchableBeanContext context)) {
            return;
        }
        List<String> stores = new ArrayList<>();
        for (BeanRegistration<InMemoryEmbeddingStoreConfig> registration : beanContext.getBeanRegistrations(InMemoryEmbeddingStoreConfig.class)) {
            InMemoryEmbeddingStoreConfig config = registration.bean();
            String name = registration.getIdentifier().getName();
            if (config != null && config.path() != null && affects(change, config.path())) {
                stores.add(name);
            }
        }
        if (stores.isEmpty()) {
            return;
        }
        boolean recreated = false;
        for (String name : stores) {
            if (context.recreate(EmbeddingStore.class, Qualifiers.byName(name))) {
                recreated = true;
                LOG.debug("Recreated the in-memory embedding store {}, since its file changed", name);
            }
        }
        if (recreated) {
            rebuildServices("in-memory embedding stores " + stores + " changed");
        }
    }

    /**
     * Whether a change touched the file of a store: a classpath path is matched against the end of the changed
     * paths, a file path against the paths themselves.
     */
    private static boolean affects(ResourceChange change, String configuredPath) {
        String path = configuredPath;
        if (path.startsWith(CLASSPATH_PREFIX)) {
            path = path.substring(CLASSPATH_PREFIX.length());
        } else if (path.startsWith(FILE_PREFIX)) {
            path = path.substring(FILE_PREFIX.length());
        }
        while (path.startsWith("/") && !configuredPath.startsWith(FILE_PREFIX)) {
            path = path.substring(1);
        }
        if (path.isEmpty()) {
            return false;
        }
        Path relative = Path.of(path);
        return touches(change.changed(), relative) || touches(change.removed(), relative);
    }

    private static boolean touches(List<Path> paths, Path configured) {
        for (Path changed : paths) {
            if (configured.isAbsolute() ? changed.toAbsolutePath().normalize().equals(configured.normalize()) : changed.endsWith(configured)) {
                return true;
            }
        }
        return false;
    }

    private static boolean changed(@Nullable List<?> added, @Nullable List<?> removed) {
        return added != null && !added.isEmpty() || removed != null && !removed.isEmpty();
    }

    /**
     * The AI service methods LangChain4j validated on a call, which it keeps in a static set of
     * {@code AiServiceValidation}: the methods of a retired generation are removed from it.
     */
    private static final class ValidatedMethods {
        private static final String VALIDATION = "dev.langchain4j.service.AiServiceValidation";
        private static final String FIELD = "VALID_METHODS";
        private static final @Nullable Set<Method> METHODS = find();

        private ValidatedMethods() {
        }

        @SuppressWarnings("unchecked")
        private static @Nullable Set<Method> find() {
            try {
                Field field = Class.forName(VALIDATION, false, AiServices.class.getClassLoader()).getDeclaredField(FIELD);
                if (Modifier.isStatic(field.getModifiers()) && Set.class.isAssignableFrom(field.getType()) && field.trySetAccessible()) {
                    return (Set<Method>) field.get(null);
                }
            } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                LOG.debug("Cannot read the AI service methods LangChain4j validated, which a retired generation may stay reachable through", e);
            }
            return null;
        }

        static void evict(ClassChangeEvent change) {
            Set<Method> methods = METHODS;
            if (methods == null || change.retiredLoaders().isEmpty()) {
                return;
            }
            // LangChain4j adds to the set without a lock: a call that adds meanwhile is retried once
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    methods.removeIf(method -> change.isStaleType(method.getDeclaringClass()));
                    return;
                } catch (ConcurrentModificationException e) {
                    // retried
                }
            }
        }
    }

    /**
     * Follows the tool methods. The first batch only says whether there are any.
     */
    private final class ToolsWatcher implements ExecutableMethodWatcher<Tool>, Ordered {
        @Override
        public void onChange(ExecutableMethodChange<Tool> change) {
            anyTools = !change.current().isEmpty();
            if (!change.initial() && changed(change.added(), change.removed())) {
                rebuildServices("tool methods changed: " + change);
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Follows the AI service definitions. The first batch only says whether there are any.
     */
    private final class ServicesWatcher implements BeanDefinitionWatcher<Object>, Ordered {
        @Override
        public void onChange(BeanDefinitionChange<Object> change) {
            anyServices = !change.current().isEmpty();
            if (!change.initial() && changed(change.added(), change.removed())) {
                rebuildServices("AI service definitions changed: " + change);
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Follows a class change applied in place that retires a class loader.
     */
    private final class ClassWatcher implements ClassChangeWatcher, Ordered {
        @Override
        public void onChange(ClassChangeEvent change) {
            ValidatedMethods.evict(change);
            if (change.strategy() == ReloadStrategy.RESTART || change.retiredLoaders().isEmpty()) {
                // a restart builds everything again; a method body changed in place runs in the tools already built
                return;
            }
            if (anyTools || anyServices) {
                rebuildServices("a reload retired a class loader");
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Follows the files of the configuration root, among which the in-memory embedding stores are read from.
     */
    private final class StoreFilesWatcher implements ResourceWatcher, Ordered {
        @Override
        public void onChange(ResourceChange change) {
            onStoreFiles(change);
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
