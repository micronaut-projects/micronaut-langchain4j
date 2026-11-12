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
import io.micronaut.context.BeanDependencyGraph;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.ConfigurableBeanContext;
import io.micronaut.context.annotation.Retain;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.reload.BeanRetentionPolicy;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.MethodInjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Refuses to retain a LangChain4j model or embedding store across a development restart when it may hold what the
 * restart does not keep. The generated factories, and the handwritten Bedrock Titan one, mark the models and stores
 * they produce with {@link Retain}. The context checks what the bean received through injection, and what the
 * configuration beans and builders it was made from received; it cannot see what a listener of the application gave
 * one of those beans as the context created it, such as a chat model listener added to a model builder, which is not
 * injected.
 *
 * <p>This policy decides from what the context knows of how the bean was made: its definition, the definitions of the
 * beans it was made from, as the dependency graph records them, and the listeners of the context. It never reads the
 * objects LangChain4j built. It refuses a bean of this module that is made from:</p>
 *
 * <ul>
 *     <li>a bean that a {@code BeanCreatedEventListener} or {@code BeanInitializedEventListener} of the application
 *     applies to, such as its builder or its configuration, whatever the listener gave it;</li>
 *     <li>a bean that received the context, a provider, the environment or the event publisher, which resolve through
 *     the stopped context, such as the Micronaut HTTP client builder of LangChain4j, which the providers use by
 *     default. The context refuses such a bean too, with a warning on every restart; it is refused here, first, and
 *     said at debug level, since it is how the models are made by default.</li>
 * </ul>
 *
 * <p>What LangChain4j itself loads from the application, such as a service provider the application declares, is not
 * seen.</p>
 *
 * <p>It abstains on any other bean. It runs in development mode only, as a context stops for a restart, and never on
 * a request.</p>
 *
 * @author graemerocher
 * @since 2.4.0
 */
@Internal
@Singleton
@DevelopmentActive
final class LangChain4jRetentionPolicy implements BeanRetentionPolicy {

    private static final Logger LOG = LoggerFactory.getLogger(LangChain4jRetentionPolicy.class);
    private static final String MODULE_PACKAGE = "io.micronaut.langchain4j.";
    private static final Class<?>[] CONTEXT_BOUND = {
        BeanLocator.class,
        BeanProvider.class,
        jakarta.inject.Provider.class,
        PropertyResolver.class,
        ApplicationEventPublisher.class
    };

    private final BeanContext beanContext;
    private final Predicate<Class<?>> isApplicationClass;
    private @Nullable ApplicationListeners listeners;

    /**
     * @param beanContext The context, whose definitions, listeners and dependency graph tell how a bean was made
     */
    @Inject
    LangChain4jRetentionPolicy(BeanContext beanContext) {
        this(beanContext, runtimeClassPredicate().negate());
    }

    /**
     * @param beanContext The context
     * @param isApplicationClass Whether a class is of the application, which the restart replaces
     */
    LangChain4jRetentionPolicy(BeanContext beanContext, Predicate<Class<?>> isApplicationClass) {
        this.beanContext = beanContext;
        this.isApplicationClass = isApplicationClass;
    }

    @Override
    public Decision decide(BeanRegistration<?> registration) {
        BeanDefinition<?> definition = registration.getBeanDefinition();
        if (definition == null || !isRetainedByThisModule(definition)) {
            return Decision.ABSTAIN;
        }
        String reason = listeners().customizing(definition);
        if (reason == null) {
            reason = contextBound(definition);
        }
        if (reason == null) {
            return Decision.ABSTAIN;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Not retaining [{}] across the restart: {}", registration.bean(), reason);
        }
        return Decision.REFUSE;
    }

    /**
     * Whether a bean is one this module marks retained: produced by a factory of this module with {@link Retain}.
     */
    private static boolean isRetainedByThisModule(BeanDefinition<?> definition) {
        return definition.getAnnotationMetadata().getDeclaredMetadata().hasStereotype(Retain.class)
            && definition.getDeclaringType().map(type -> type.getName().startsWith(MODULE_PACKAGE)).orElse(false);
    }

    private synchronized ApplicationListeners listeners() {
        ApplicationListeners found = listeners;
        if (found == null) {
            // read as the context stops, which is when the policy is asked, so that a listener added meanwhile counts
            found = new ApplicationListeners(beanContext, isApplicationClass);
            listeners = found;
        }
        return found;
    }

    /**
     * The first bean the bean was made from, itself included, that received what resolves through the stopped
     * context, as its injection points declare it.
     */
    private @Nullable String contextBound(BeanDefinition<?> definition) {
        Set<BeanDefinition<?>> madeFrom = new LinkedHashSet<>();
        madeFrom.add(definition);
        BeanDependencyGraph graph = beanContext instanceof ConfigurableBeanContext configurable ? configurable.findDependencyGraph().orElse(null) : null;
        if (graph != null) {
            madeFrom.addAll(graph.transitiveDependenciesOf(definition));
        }
        for (BeanDefinition<?> candidate : madeFrom) {
            Class<?> bound = receivedContextBound(candidate);
            if (bound != null) {
                return (candidate == definition ? "it receives " : "the bean " + candidate.getBeanType().getName() + " it is made from receives ")
                    + bound.getName() + ", which resolves through the stopped context";
            }
        }
        return null;
    }

    private static @Nullable Class<?> receivedContextBound(BeanDefinition<?> definition) {
        for (Argument<?> argument : definition.getConstructor().getArguments()) {
            if (isContextBound(argument.getType())) {
                return argument.getType();
            }
        }
        for (MethodInjectionPoint<?, ?> method : definition.getInjectedMethods()) {
            for (Argument<?> argument : method.getArguments()) {
                if (isContextBound(argument.getType())) {
                    return argument.getType();
                }
            }
        }
        for (FieldInjectionPoint<?, ?> field : definition.getInjectedFields()) {
            if (isContextBound(field.getType())) {
                return field.getType();
            }
        }
        return null;
    }

    private static boolean isContextBound(Class<?> type) {
        for (Class<?> bound : CONTEXT_BOUND) {
            if (bound.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a class was loaded by the class loader of this module, one of its parents, or the platform: the classes
     * of the application are loaded by a child of the loader of the libraries, which a restart replaces.
     */
    private static Predicate<Class<?>> runtimeClassPredicate() {
        Set<ClassLoader> loaders = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ClassLoader loader = LangChain4jRetentionPolicy.class.getClassLoader(); loader != null; loader = loader.getParent()) {
            loaders.add(loader);
        }
        for (ClassLoader loader = ClassLoader.getPlatformClassLoader(); loader != null; loader = loader.getParent()) {
            loaders.add(loader);
        }
        return type -> type.getClassLoader() == null || loaders.contains(type.getClassLoader());
    }
}
