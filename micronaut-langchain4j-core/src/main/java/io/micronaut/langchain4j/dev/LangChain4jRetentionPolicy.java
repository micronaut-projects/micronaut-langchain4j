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

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.reload.BeanRetentionPolicy;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.value.PropertyResolver;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Refuses to retain a LangChain4j model, embedding store or builder across a development restart when what it holds
 * would not survive the restart. The generated factories mark the models and stores they produce with
 * {@link io.micronaut.context.annotation.Retain}, and the context checks what the bean received; but a model is built
 * by a LangChain4j builder from a configuration bean, which the context leaves out of the retained closure, so it does
 * not see what the configuration bean handed to the builder. This policy looks at the objects the bean holds instead,
 * and refuses it when it reaches:
 *
 * <ul>
 *     <li>an object, a class or a class loader of the application, which a restart replaces: a listener, a tokenizer
 *     or an HTTP client builder of the application, for example;</li>
 *     <li>the context, a provider, the environment or the event publisher, which resolve through the stopped context:
 *     the Micronaut HTTP client of LangChain4j resolves its client through them, so a model built on it is created
 *     again in each generation, on the event loops the development runtime keeps;</li>
 *     <li>another singleton of the context, which the context destroys as it stops, such as a client bean that was
 *     injected into the configuration.</li>
 * </ul>
 *
 * <p>It looks only at beans whose class is one of LangChain4j's, and abstains on any other. It runs in development
 * mode only, as a context stops for a restart, and never on a request.</p>
 *
 * @author graemerocher
 * @since 2.4.0
 */
@Internal
@Singleton
@DevelopmentActive
final class LangChain4jRetentionPolicy implements BeanRetentionPolicy {

    private static final Logger LOG = LoggerFactory.getLogger(LangChain4jRetentionPolicy.class);
    private static final String LANGCHAIN4J_PACKAGE = "dev.langchain4j.";
    /**
     * Past this many objects the bean is refused rather than read further: a graph this large is not a client.
     */
    private static final int MAX_OBJECTS = 200_000;
    private static final Class<?>[] CONTEXT_BOUND = {
        BeanLocator.class,
        BeanProvider.class,
        jakarta.inject.Provider.class,
        PropertyResolver.class,
        ApplicationEventPublisher.class
    };

    private final BeanContext beanContext;
    private final Set<ClassLoader> runtimeLoaders;
    private final Map<Object, Decision> decisions = Collections.synchronizedMap(new IdentityHashMap<>());
    private final AtomicReference<@Nullable Set<Object>> singletons = new AtomicReference<>();

    /**
     * @param beanContext The context, whose singletons a retained bean must not hold
     */
    LangChain4jRetentionPolicy(BeanContext beanContext) {
        this.beanContext = beanContext;
        Set<ClassLoader> loaders = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ClassLoader loader = LangChain4jRetentionPolicy.class.getClassLoader(); loader != null; loader = loader.getParent()) {
            loaders.add(loader);
        }
        ClassLoader platform = ClassLoader.getPlatformClassLoader();
        for (ClassLoader loader = platform; loader != null; loader = loader.getParent()) {
            loaders.add(loader);
        }
        this.runtimeLoaders = loaders;
    }

    @Override
    public Decision decide(BeanRegistration<?> registration) {
        Object bean = registration.bean();
        if (bean == null || !bean.getClass().getName().startsWith(LANGCHAIN4J_PACKAGE)) {
            return Decision.ABSTAIN;
        }
        return decisions.computeIfAbsent(bean, this::inspect);
    }

    private Decision inspect(Object bean) {
        String reason = new Walk(bean).firstReason();
        if (reason == null) {
            return Decision.ABSTAIN;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Not retaining [{}] across the restart: it holds {}", bean, reason);
        }
        return Decision.REFUSE;
    }

    private Set<Object> singletons() {
        Set<Object> current = singletons.get();
        if (current == null) {
            Set<Object> instances = Collections.newSetFromMap(new IdentityHashMap<>());
            for (BeanRegistration<Object> registration : beanContext.getActiveBeanRegistrations(Object.class)) {
                Object instance = registration.bean();
                if (instance != null) {
                    instances.add(instance);
                }
            }
            singletons.compareAndSet(null, instances);
            current = singletons.get();
        }
        return current;
    }

    private boolean isRuntime(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return loader == null || runtimeLoaders.contains(loader);
    }

    private static boolean isJdk(Class<?> type) {
        String name = type.getName();
        return type.getClassLoader() == null
            || type.getClassLoader() == ClassLoader.getPlatformClassLoader()
            || name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") || name.startsWith("sun.");
    }

    /**
     * Reads the objects a bean holds, breadth first, and stops at the first that keeps it from being retained.
     */
    private final class Walk {
        private final Object root;
        private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Deque<Object> pending = new ArrayDeque<>();

        private Walk(Object root) {
            this.root = root;
        }

        @Nullable String firstReason() {
            pending.add(root);
            while (!pending.isEmpty()) {
                if (visited.size() > MAX_OBJECTS) {
                    return "more than " + MAX_OBJECTS + " objects";
                }
                Object current = pending.poll();
                if (!visited.add(current)) {
                    continue;
                }
                String reason = check(current);
                if (reason != null) {
                    return reason;
                }
                expand(current);
            }
            return null;
        }

        private @Nullable String check(Object object) {
            if (object instanceof Class<?> type) {
                return isRuntime(type) ? null : "the application class " + type.getName();
            }
            if (object instanceof ClassLoader loader) {
                return runtimeLoaders.contains(loader) ? null : "the application class loader " + loader;
            }
            Class<?> type = object.getClass();
            if (!isRuntime(type)) {
                return "an instance of the application class " + type.getName();
            }
            for (Class<?> bound : CONTEXT_BOUND) {
                if (bound.isInstance(object)) {
                    return "an instance of " + type.getName() + ", which is bound to the stopped context";
                }
            }
            if (object != root && singletons().contains(object)) {
                return "the singleton " + type.getName() + " of the stopped context, which it destroys";
            }
            return null;
        }

        private void expand(Object object) {
            Class<?> type = object.getClass();
            if (type.isArray()) {
                if (!type.getComponentType().isPrimitive()) {
                    int length = Array.getLength(object);
                    for (int i = 0; i < length; i++) {
                        add(Array.get(object, i));
                    }
                }
                return;
            }
            if (isJdk(type)) {
                expandJdk(object);
                return;
            }
            for (Class<?> declaring = type; declaring != null && !isJdk(declaring); declaring = declaring.getSuperclass()) {
                for (Field field : declaring.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
                        continue;
                    }
                    if (!field.trySetAccessible()) {
                        continue;
                    }
                    try {
                        add(field.get(object));
                    } catch (IllegalAccessException | RuntimeException e) {
                        // not readable: nothing to check through it
                    }
                }
            }
        }

        /**
         * The JDK's own classes are not read field by field, which their modules refuse, but the containers among
         * them are read through their API.
         */
        private void expandJdk(Object object) {
            try {
                if (object instanceof Collection<?> collection) {
                    for (Object element : collection.toArray()) {
                        add(element);
                    }
                } else if (object instanceof Map<?, ?> map) {
                    for (Object entry : map.entrySet().toArray()) {
                        if (entry instanceof Map.Entry<?, ?> mapEntry) {
                            add(mapEntry.getKey());
                            add(mapEntry.getValue());
                        }
                    }
                } else if (object instanceof Optional<?> optional) {
                    optional.ifPresent(this::add);
                } else if (object instanceof AtomicReference<?> reference) {
                    add(reference.get());
                }
            } catch (RuntimeException e) {
                // a container that cannot be read as a whole, such as one modified meanwhile: nothing more is read
            }
        }

        private void add(@Nullable Object object) {
            if (object != null && !visited.contains(object)) {
                pending.add(object);
            }
        }
    }
}
