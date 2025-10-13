/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.agentic;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.agent.AgentBuilder;
import dev.langchain4j.agentic.workflow.ConditionalAgentService;
import dev.langchain4j.agentic.workflow.LoopAgentService;
import dev.langchain4j.agentic.workflow.ParallelAgentService;
import dev.langchain4j.agentic.workflow.SequentialAgentService;
import dev.langchain4j.agentic.workflow.WorkflowAgentsBuilder;
import dev.langchain4j.agentic.workflow.impl.ConditionalAgentServiceImpl;
import dev.langchain4j.agentic.workflow.impl.LoopAgentServiceImpl;
import dev.langchain4j.agentic.workflow.impl.ParallelAgentServiceImpl;
import dev.langchain4j.agentic.workflow.impl.SequentialAgentServiceImpl;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.RuntimeBeanDefinition;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.tools.ToolRegistry;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.micronaut.langchain4j.agentic.AgenticServiceInterceptor.resolveAgentInterface;

/**
 * Factory for building agentic service proxies using LangChain4j AgenticServices and Micronaut BeanContext.
 */
@Factory
public final class AgenticServiceFactory {

    public static final String AGENTIC_CONFIG_PREFIX = "langchain4j.agentic.agents.";
    @SuppressWarnings("rawtypes")
    private static final Argument<BeanCreatedEventListener> BEAN_CREATED_EVENT_LISTENER_ARGUMENT = Argument.of(BeanCreatedEventListener.class, Argument.of(AgentBuilder.class));

    private final Lock lock = new ReentrantLock();

    /**
     * Build the agentic service for the given definition.
     *
     * @param beanContext Micronaut BeanContext
     * @param serviceDef Service definition information for building an agentic proxy
     * @return the agentic proxy (instance that should be used as target of all method calls)
     */
    public Object buildAgenticService(BeanContext beanContext,
                                      AgenticServiceInfo<Object> serviceDef) {

        var iface = resolveAgentInterface(serviceDef.type());
        var env = beanContext.getBean(Environment.class);
        var agentName =
            (serviceDef.name() != null && !serviceDef.name().isBlank()) ? serviceDef.name() :
                deriveAgentName(iface);

        var chatModelBean = resolveChatModelForAgent(beanContext, env, agentName, iface);
        lock.lock();
        try {
            // Build declarative agentic system using a Micronaut-backed WorkflowAgentsBuilder so workflow services are beans
            AgenticServices.setWorkflowAgentsBuilder(
                new MicronautWorkflowAgentsBuilder(beanContext));
            try {
                //noinspection unchecked
                return AgenticServices.createAgenticSystem((Class) iface, chatModelBean,
                    ctx -> {
                        applyDefaultBuilderConfig(beanContext, ctx, iface, serviceDef);
                        for (var beanCreatedEventListener : beanContext.getBeansOfType(BEAN_CREATED_EVENT_LISTENER_ARGUMENT)) {
                            callBeanCreatedEventListeners(beanContext, ctx, beanCreatedEventListener);
                        }
                    });
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "AgenticServices.createAgenticSystem failed for " + iface.getName(), e);
            } finally {
                AgenticServices.setWorkflowAgentsBuilder(null);
            }
        } finally {
            lock.unlock();
        }
    }

    private static void callBeanCreatedEventListeners(BeanContext beanContext,
                                  AgenticServices.DeclarativeAgentCreationContext<?> ctx,
                                  BeanCreatedEventListener<?> beanCreatedEventListener) {
        //noinspection unchecked
        beanCreatedEventListener.onCreated(new BeanCreatedEvent(
            beanContext,
            new RuntimeBeanDefinition<AgentBuilder<?>>() {
                @Override
                public @NonNull AgentBuilder<?> instantiate(
                    @NonNull BeanResolutionContext resolutionContext,
                    @NonNull BeanContext context)
                    throws BeanInstantiationException {
                    return ctx.agentBuilder();
                }

                @Override
                public @NonNull Class<AgentBuilder<?>> getBeanType() {
                    //noinspection unchecked
                    return (Class<AgentBuilder<?>>) ctx.agentBuilder()
                        .getClass();
                }
            },
            BeanIdentifier.of(deriveAgentName(ctx.agentServiceClass())),
            Argument.of(AgentBuilder.class),
            ctx.agentBuilder()
        ));
    }

    // Derive default agent id from interface name, e.g. GreeterAgent -> greeter
    private static String deriveAgentName(Class<?> type) {
        var simple = type.getSimpleName();
        var dollar = simple.indexOf('$');
        if (dollar >= 0) {
            // Handle Micronaut intercepted classes like GreeterAgent$Intercepted
            simple = simple.substring(0, dollar);
        }
        if (simple.endsWith("Agent")) {
            simple = simple.substring(0, simple.length() - "Agent".length());
        }
        // camelCase to kebab-case
        var kebab = simple.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
            .replaceAll("([A-Z])([A-Z][a-z])", "$1-$2").toLowerCase();
        return kebab;
    }

    // Convert dashed-name to camelCase (ex: friendly-chat-model => friendlyChatModel)
    private static String toCamelCase(String name) {
        var sb = new StringBuilder();
        var capitalize = false;
        for (var c : name.toCharArray()) {
            if (c == '-' || c == '_') {
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Resolve the ChatModel bean based on an optional bean name, or sensible defaults.
     * Aligns with AiServiceFactory logic by avoiding NonUniqueBeanException and preferring any available bean
     * when no explicit name is provided.
     */
    @Nullable
    private static Object resolveChatModel(BeanContext beanContext,
                                           @Nullable String chatModelBeanName) {
        var provider = beanContext.getProvider(ChatModel.class);

        // If a specific bean name is provided, try it first (and its camelCase variant).
        if (chatModelBeanName != null && !chatModelBeanName.isBlank()) {
            var byName = provider.find(Qualifiers.byName(chatModelBeanName));
            if (byName.isPresent()) {
                return byName.get();
            }
            var byCamel = provider.find(Qualifiers.byName(toCamelCase(chatModelBeanName)));
            if (byCamel.isPresent()) {
                return byCamel.get();
            }
            // Fall back to any available ChatModel (e.g., @Primary or the only one)
            if (provider.isPresent()) {
                return provider.get();
            }
            return null;
        }

        // No explicit name: prefer any available ChatModel (e.g., @Primary or the only one)
        if (provider.isPresent()) {
            return provider.get();
        }
        // As a last resort, pick the first bean if multiple are present but the provider cannot resolve directly
        var all = beanContext.getBeansOfType(ChatModel.class);
        if (!all.isEmpty()) {
            return all.iterator().next();
        }
        return null;
    }

    /**
     * Resolve ChatModel by agent name or interface type.
     * If agentNameOverride is provided, it takes precedence over the derived name from type.
     */
    @Nullable
    private static ChatModel resolveChatModelForAgent(BeanContext beanContext,
                                                      Environment env,
                                                      @Nullable String agentNameOverride,
                                                      @Nullable Class<?> agentType) {
        var name = agentNameOverride;
        if ((name == null || name.isBlank()) && agentType != null) {
            name = deriveAgentName(agentType);
        }
        if (name != null && !name.isBlank()) {
            var path = AGENTIC_CONFIG_PREFIX + name + ".chat-model";
            var beanName = env.containsProperty(path)
                ? env.getProperty(path, String.class).orElse(null)
                : null;
            return (ChatModel) resolveChatModel(beanContext, beanName);
        }
        return (ChatModel) resolveChatModel(beanContext, null);
    }

    /**
     * Resolve ChatModel for sub-agents in a safe way:
     * - If a specific property is defined for the sub-agent, resolve that named bean.
     * - If no property is defined, return null to avoid ambiguous unqualified lookups.
     * This prevents NonUniqueBeanException in environments with multiple ChatModel beans.
     */
    @Nullable
    private static ChatModel resolveChatModelForSubAgent(BeanContext beanContext,
                                                         Environment env,
                                                         Class<?> agentType) {
        var name = deriveAgentName(agentType);
        var path = AGENTIC_CONFIG_PREFIX + name + ".chat-model";
        var beanName = env.containsProperty(path)
            ? env.getProperty(path, String.class).orElse(null)
            : null;
        return (ChatModel) resolveChatModel(beanContext, beanName);
    }

    private static MessageWindowChatMemory resolveChatMemoryForAgent(BeanContext beanContext,
                                                                     Environment env,
                                                                     Class<?> agentType) {
        var name = deriveAgentName(agentType);
        var maxMessagesPath = AGENTIC_CONFIG_PREFIX + name + ".memory.max-messages";
        var storeNamePath = AGENTIC_CONFIG_PREFIX + name + ".memory.store";
        var maxMessages = env.getProperty(maxMessagesPath, Integer.class).orElse(null);
        var storeName = env.getProperty(storeNamePath, String.class).orElse(null);
        return buildChatMemory(beanContext, maxMessages, storeName);
    }

    private static MessageWindowChatMemory buildChatMemory(BeanContext beanContext,
                                                           @Nullable Integer overrideMaxMessages,
                                                           @Nullable String storeBeanName) {
        MessageWindowChatMemory.Builder builder = null;

        if (storeBeanName != null && !storeBeanName.isBlank()) {
            var byName = beanContext.findBean(MessageWindowChatMemory.Builder.class, Qualifiers.byName(storeBeanName));
            if (byName.isEmpty()) {
                throw new IllegalStateException("No MessageWindowChatMemory.Builder bean found for store name '" + storeBeanName + "'. Ensure a ChatMemoryStore bean with that @Named qualifier exists.");
            }
            builder = byName.get();
        } else {
            // Try default resolution first (handles @Primary or single candidate)
            var defaultBuilder = beanContext.findBean(MessageWindowChatMemory.Builder.class);
            if (defaultBuilder.isPresent()) {
                builder = defaultBuilder.get();
            } else {
                var builders = beanContext.getBeansOfType(MessageWindowChatMemory.Builder.class);
                if (builders.size() == 1) {
                    builder = builders.iterator().next();
                }
            }
        }

        if (builder == null) {
            // Do not arbitrarily pick a store; guide users to configure the agent-specific store name.
            throw new IllegalStateException("Unable to resolve a chat memory builder. Multiple ChatMemoryStore beans may be present. Configure '"
                + AGENTIC_CONFIG_PREFIX + "<agent-name>.memory.store' with the desired bean name.");
        }

        if (overrideMaxMessages != null) {
            builder = builder.maxMessages(overrideMaxMessages);
        }
        return builder.build();
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected AgentBuilder<?> agentBuilder(@Parameter Class<?> agentServiceClass) {
        return AgenticServices.agentBuilder((Class) agentServiceClass);
    }

    /**
     * Factory method producing LoopAgentService instances. It is intentionally parameterized with an optional
     * agent service class to support both typed and untyped builders while emitting BeanCreatedEvent.
     *
     * @param agentServiceClass optional agent interface class to create a typed builder for
     * @return a LoopAgentService builder for typed or untyped agents
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected LoopAgentService<?> loopAgentService(
        @Nullable @Parameter Class<?> agentServiceClass) {
        if (agentServiceClass != null) {
            return LoopAgentServiceImpl.builder(agentServiceClass);
        }
        return LoopAgentServiceImpl.builder();
    }

    /**
     * Factory method producing SequentialAgentService instances. Supports typed and untyped variants.
     *
     * @param agentServiceClass optional agent interface class to create a typed builder for
     * @return a SequentialAgentService builder for typed or untyped agents
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected SequentialAgentService<?> sequentialAgentService(
        @Nullable @Parameter Class<?> agentServiceClass) {
        if (agentServiceClass != null) {
            return SequentialAgentServiceImpl.builder(agentServiceClass);
        }
        return SequentialAgentServiceImpl.builder();
    }

    /**
     * Factory method producing ParallelAgentService instances. Supports typed and untyped variants.
     *
     * @param agentServiceClass optional agent interface class to create a typed builder for
     * @return a ParallelAgentService builder for typed or untyped agents
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ParallelAgentService<?> parallelAgentService(
        @Nullable @Parameter Class<?> agentServiceClass) {
        if (agentServiceClass != null) {
            return ParallelAgentServiceImpl.builder(agentServiceClass);
        }
        return ParallelAgentServiceImpl.builder();
    }

    /**
     * Factory method producing ConditionalAgentService instances. Supports typed and untyped variants.
     *
     * @param agentServiceClass optional agent interface class to create a typed builder for
     * @return a ConditionalAgentService builder for typed or untyped agents
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ConditionalAgentService<?> conditionalAgentService(
        @Nullable @Parameter Class<?> agentServiceClass) {
        if (agentServiceClass != null) {
            return ConditionalAgentServiceImpl.builder(agentServiceClass);
        }
        return ConditionalAgentServiceImpl.builder();
    }

    /**
     * Apply default chat model and memory to the builder of a (sub-)agent based on its declaring type.
     * This shares the same resolution logic for top-level agents and sub-agents.
     */
    private static void applyDefaultBuilderConfig(BeanContext beanContext,
                                                  AgenticServices.DeclarativeAgentCreationContext<?> ctx,
                                                  Class<?> iface,
                                                  AgenticServiceInfo<Object> serviceDef) {
        var env = beanContext.getBean(Environment.class);
        // Resolve chat model for sub-agents only when explicitly configured.
        // Default chat model is provided by LC4J (top-level), keeping behavior consistent and avoiding ambiguity.
        var model = resolveChatModelForSubAgent(beanContext, env, ctx.agentServiceClass());
        var ab = ctx.agentBuilder();
        if (model != null) {
            ab.chatModel(model);
        }
        // Resolve memory with default and per-agent override (max-messages)
        var memory = resolveChatMemoryForAgent(beanContext, env, ctx.agentServiceClass());
        ab.chatMemoryProvider(unused -> memory);
        // For top-level agent only, ensure ToolRegistry is consulted for tools declared on @AgenticService
        if (ctx.agentServiceClass() == iface) {
            var toolTypes = serviceDef.tools();
            if (toolTypes != null && !toolTypes.isEmpty()) {
                var registryOpt = beanContext.findBean(ToolRegistry.class);
                registryOpt.ifPresent(reg -> reg.getToolsTyped(toolTypes));
            }
        }
    }

    /**
     * Micronaut-backed WorkflowAgentsBuilder that creates workflow services as Micronaut beans,
     * so BeanCreatedEventListener hooks (e.g., for LoopAgentService) can intercept and customize them.
     */
    static final class MicronautWorkflowAgentsBuilder implements WorkflowAgentsBuilder {
        private final BeanContext beanContext;

        MicronautWorkflowAgentsBuilder(BeanContext beanContext) {
            this.beanContext = beanContext;
        }

        @SuppressWarnings("unchecked")
        @Override
        public LoopAgentService<UntypedAgent> loopBuilder() {
            // Create an untyped LoopAgentService as a Micronaut bean (fires BeanCreatedEvent)
            return (LoopAgentService<UntypedAgent>) beanContext.createBean(LoopAgentService.class,
                (Class<?>) null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> LoopAgentService<T> loopBuilder(Class<T> agentServiceClass) {
            // Create a typed LoopAgentService as a Micronaut bean (fires BeanCreatedEvent)
            return (LoopAgentService<T>) beanContext.createBean(LoopAgentService.class,
                agentServiceClass);
        }

        // Sequence builders
        @SuppressWarnings("unchecked")
        @Override
        public SequentialAgentService<UntypedAgent> sequenceBuilder() {
            return (SequentialAgentService<UntypedAgent>) beanContext.createBean(
                SequentialAgentService.class, (Class<?>) null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> SequentialAgentService<T> sequenceBuilder(Class<T> agentServiceClass) {
            return (SequentialAgentService<T>) beanContext.createBean(SequentialAgentService.class,
                agentServiceClass);
        }

        // Parallel builders
        @SuppressWarnings("unchecked")
        @Override
        public ParallelAgentService<UntypedAgent> parallelBuilder() {
            return (ParallelAgentService<UntypedAgent>) beanContext.createBean(
                ParallelAgentService.class, (Class<?>) null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> ParallelAgentService<T> parallelBuilder(Class<T> agentServiceClass) {
            return (ParallelAgentService<T>) beanContext.createBean(ParallelAgentService.class,
                agentServiceClass);
        }

        // Conditional builders
        @SuppressWarnings("unchecked")
        @Override
        public ConditionalAgentService<UntypedAgent> conditionalBuilder() {
            return (ConditionalAgentService<UntypedAgent>) beanContext.createBean(
                ConditionalAgentService.class, (Class<?>) null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> ConditionalAgentService<T> conditionalBuilder(Class<T> agentServiceClass) {
            return (ConditionalAgentService<T>) beanContext.createBean(
                ConditionalAgentService.class, agentServiceClass);
        }

        // Other workflow builders (e.g., supervisor) can be added here
        // to provide full DI-managed lifecycle when we need them.
    }
}
