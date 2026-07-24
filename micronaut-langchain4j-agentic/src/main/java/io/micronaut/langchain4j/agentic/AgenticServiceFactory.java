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
package io.micronaut.langchain4j.agentic;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.agent.AgentBuilder;
import dev.langchain4j.agentic.workflow.ConditionalAgentService;
import dev.langchain4j.agentic.workflow.LoopAgentService;
import dev.langchain4j.agentic.workflow.ParallelAgentService;
import dev.langchain4j.agentic.workflow.ParallelMapperService;
import dev.langchain4j.agentic.workflow.SequentialAgentService;
import dev.langchain4j.agentic.workflow.WorkflowAgentsBuilder;
import dev.langchain4j.agentic.workflow.impl.ConditionalAgentServiceImpl;
import dev.langchain4j.agentic.workflow.impl.LoopAgentServiceImpl;
import dev.langchain4j.agentic.workflow.impl.ParallelAgentServiceImpl;
import dev.langchain4j.agentic.workflow.impl.ParallelMapperServiceImpl;
import dev.langchain4j.agentic.workflow.impl.SequentialAgentServiceImpl;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.RuntimeBeanDefinition;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.context.exceptions.NonUniqueBeanException;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanIdentifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.langchain4j.tools.ToolRegistry;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static io.micronaut.langchain4j.agentic.AgenticServiceInterceptor.resolveAgentInterface;

/**
 * Factory for building LangChain4j agentic service proxies.
 */
@Factory
@Experimental
public final class AgenticServiceFactory {

    public static final String AGENTIC_CONFIG_PREFIX = AgentConfiguration.PREFIX + '.';

    @SuppressWarnings("rawtypes")
    private static final Argument<BeanCreatedEventListener> AGENT_BUILDER_LISTENER = Argument.of(
        BeanCreatedEventListener.class,
        Argument.of(AgentBuilder.class)
    );

    private final Lock lock = new ReentrantLock();

    /**
     * Build the agentic service for the given definition.
     *
     * @param beanContext Micronaut BeanContext
     * @param serviceDef Service definition information for building an agentic proxy
     * @return the agentic proxy
     */
    public Object buildAgenticService(BeanContext beanContext,
                                      AgenticServiceInfo<Object> serviceDef) {
        Class<?> iface = resolveAgentInterface(serviceDef.type());
        ChatModel chatModel = resolveRootChatModel(beanContext, serviceDef, iface);

        lock.lock();
        try {
            AgenticServices.setWorkflowAgentsBuilder(new MicronautWorkflowAgentsBuilder(beanContext));
            try {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Object agent = AgenticServices.createAgenticSystem(
                    (Class) iface,
                    chatModel,
                    new AgenticServices.AgentConfigurator(ctx -> {
                        applyBuilderConfig(beanContext, serviceDef, iface, ctx);
                        fireAgentBuilderListeners(beanContext, ctx);
                    }, null, null)
                );
                return agent;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("AgenticServices.createAgenticSystem failed for " + iface.getName(), e);
            } finally {
                AgenticServices.setWorkflowAgentsBuilder(null);
            }
        } finally {
            lock.unlock();
        }
    }

    private static void fireAgentBuilderListeners(BeanContext beanContext,
                                                  AgenticServices.DeclarativeAgentCreationContext<?> ctx) {
        for (BeanCreatedEventListener<?> listener : beanContext.getBeansOfType(AGENT_BUILDER_LISTENER)) {
            callBeanCreatedEventListener(beanContext, ctx, listener);
        }
    }

    private static void callBeanCreatedEventListener(BeanContext beanContext,
                                                     AgenticServices.DeclarativeAgentCreationContext<?> ctx,
                                                     BeanCreatedEventListener beanCreatedEventListener) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        BeanCreatedEventListener listener = beanCreatedEventListener;
        listener.onCreated(new BeanCreatedEvent(
            beanContext,
            new RuntimeBeanDefinition<AgentBuilder>() {
                @Override
                public @NonNull AgentBuilder instantiate(
                    @NonNull BeanResolutionContext resolutionContext,
                    @NonNull BeanContext context)
                    throws BeanInstantiationException {
                    return ctx.agentBuilder();
                }

                @Override
                public @NonNull Class<AgentBuilder> getBeanType() {
                    @SuppressWarnings("unchecked")
                    Class<AgentBuilder> type = (Class<AgentBuilder>) ctx.agentBuilder().getClass();
                    return type;
                }
            },
            BeanIdentifier.of(deriveAgentName(ctx.agentServiceClass())),
            Argument.of(AgentBuilder.class),
            ctx.agentBuilder()
        ));
    }

    private static void applyBuilderConfig(BeanContext beanContext,
                                           AgenticServiceInfo<Object> serviceDef,
                                           Class<?> rootInterface,
                                           AgenticServices.DeclarativeAgentCreationContext<?> ctx) {
        Class<?> agentType = resolveAgentInterface(ctx.agentServiceClass());
        String agentName = deriveAgentName(agentType);
        AgentConfiguration agentConfiguration = resolveAgentConfiguration(beanContext, agentName);
        AgentBuilder<?, ?> agentBuilder = ctx.agentBuilder();

        ChatModel configuredModel = resolveConfiguredChatModel(beanContext, agentConfiguration);
        if (configuredModel != null) {
            agentBuilder.chatModel(configuredModel);
        }

        configureMemory(beanContext, agentName, agentConfiguration, agentBuilder);
        configureRag(beanContext, agentName, agentBuilder);
        configureTools(beanContext, serviceDef, rootInterface, agentType, agentName, agentBuilder);
        configureOutputKey(serviceDef, rootInterface, agentType, agentBuilder);
    }

    private static void configureOutputKey(AgenticServiceInfo<Object> serviceDef,
                                           Class<?> rootInterface,
                                           Class<?> agentType,
                                           AgentBuilder<?, ?> agentBuilder) {
        String outputKey = null;
        if (rootInterface.equals(agentType)) {
            outputKey = serviceDef.outputKey();
        }
        AgenticService annotation = agentType.getAnnotation(AgenticService.class);
        if (annotation != null && !annotation.outputKey().isBlank()) {
            outputKey = annotation.outputKey();
        }
        if (outputKey != null && !outputKey.isBlank()) {
            agentBuilder.outputKey(outputKey);
        }
    }

    private static void configureTools(BeanContext beanContext,
                                       AgenticServiceInfo<Object> serviceDef,
                                       Class<?> rootInterface,
                                       Class<?> agentType,
                                       String agentName,
                                       AgentBuilder<?, ?> agentBuilder) {
        Set<Class<?>> toolTypes = new LinkedHashSet<>();
        if (rootInterface.equals(agentType) && serviceDef.tools() != null) {
            toolTypes.addAll(serviceDef.tools());
        }
        AgenticService annotation = agentType.getAnnotation(AgenticService.class);
        if (annotation != null) {
            toolTypes.addAll(Set.of(annotation.tools()));
        }

        if (CollectionUtils.isNotEmpty(toolTypes)) {
            beanContext.findBean(ToolRegistry.class)
                .map(registry -> registry.getToolsTyped(toolTypes))
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(tools -> agentBuilder.tools(tools.toArray()));
        }
        lookupByNameOrDefault(beanContext, agentName, ToolProvider.class, agentBuilder::toolProvider);
    }

    private static void configureMemory(BeanContext beanContext,
                                        String agentName,
                                        @Nullable AgentConfiguration agentConfiguration,
                                        AgentBuilder<?, ?> agentBuilder) {
        AgentConfiguration.MemoryConfiguration memoryConfiguration = agentConfiguration != null ? agentConfiguration.getMemory() : null;
        String storeName = memoryConfiguration != null ? memoryConfiguration.getStore() : null;
        Integer maxMessages = memoryConfiguration != null ? memoryConfiguration.getMaxMessages() : null;
        boolean memoryConfigured = storeName != null && !storeName.isBlank() || maxMessages != null;
        if (memoryConfigured || findChatMemoryBuilder(beanContext, null) != null) {
            agentBuilder.chatMemoryProvider(memoryId -> buildChatMemory(beanContext, storeName, maxMessages, memoryId));
            return;
        }
        lookupByNameOrDefault(beanContext, agentName, ChatMemoryProvider.class, agentBuilder::chatMemoryProvider);
    }

    private static MessageWindowChatMemory buildChatMemory(BeanContext beanContext,
                                                           @Nullable String storeName,
                                                           @Nullable Integer maxMessages,
                                                           @Nullable Object memoryId) {
        MessageWindowChatMemory.Builder builder = resolveChatMemoryBuilder(beanContext, storeName);
        builder.id(memoryId);
        if (maxMessages != null) {
            builder.maxMessages(maxMessages);
        }
        return builder.build();
    }

    private static MessageWindowChatMemory.Builder resolveChatMemoryBuilder(BeanContext beanContext,
                                                                            @Nullable String storeName) {
        MessageWindowChatMemory.Builder builder = findChatMemoryBuilder(beanContext, storeName);
        if (builder != null) {
            return builder;
        }
        if (storeName != null && !storeName.isBlank()) {
            throw new IllegalStateException("No MessageWindowChatMemory.Builder bean found for store name '" + storeName + "'");
        }
        throw new IllegalStateException("No default MessageWindowChatMemory.Builder bean found");
    }

    @Nullable
    private static MessageWindowChatMemory.Builder findChatMemoryBuilder(BeanContext beanContext,
                                                                         @Nullable String storeName) {
        BeanProvider<MessageWindowChatMemory.Builder> provider = beanContext.getProvider(MessageWindowChatMemory.Builder.class);
        try {
            if (storeName != null && !storeName.isBlank()) {
                return provider.find(Qualifiers.byName(storeName)).orElse(null);
            }
            return provider.find(null).orElse(null);
        } catch (NonUniqueBeanException _) {
            return null;
        }
    }

    private static void configureRag(BeanContext beanContext,
                                     String agentName,
                                     AgentBuilder<?, ?> agentBuilder) {
        lookupByNameOrDefault(beanContext, agentName, RetrievalAugmentor.class, agentBuilder::retrievalAugmentor);
        ContentRetriever contentRetriever = resolveByNameOrDefault(beanContext, agentName, ContentRetriever.class);
        if (contentRetriever != null) {
            agentBuilder.contentRetriever(contentRetriever);
            return;
        }
        EmbeddingModel embeddingModel = resolveByNameOrDefault(beanContext, agentName, EmbeddingModel.class);
        EmbeddingStore<TextSegment> embeddingStore = resolveByNameOrDefault(
            beanContext,
            agentName,
            Argument.of(EmbeddingStore.class, TextSegment.class)
        );
        if (embeddingModel != null && embeddingStore != null) {
            agentBuilder.contentRetriever(new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel));
        }
    }

    @Nullable
    private static ChatModel resolveRootChatModel(BeanContext beanContext,
                                                  AgenticServiceInfo<Object> serviceDef,
                                                  Class<?> rootInterface) {
        String name = serviceDef.name();
        if (name != null && !name.isBlank()) {
            ChatModel namedModel = resolveChatModel(beanContext, name);
            if (namedModel != null) {
                return namedModel;
            }
        }
        AgentConfiguration agentConfiguration = resolveAgentConfiguration(beanContext, deriveAgentName(rootInterface));
        ChatModel configuredModel = resolveConfiguredChatModel(beanContext, agentConfiguration);
        return configuredModel != null ? configuredModel : resolveChatModel(beanContext, null);
    }

    @Nullable
    private static ChatModel resolveConfiguredChatModel(BeanContext beanContext,
                                                        @Nullable AgentConfiguration agentConfiguration) {
        String beanName = agentConfiguration != null ? agentConfiguration.getChatModel() : null;
        return beanName == null || beanName.isBlank() ? null : resolveChatModel(beanContext, beanName);
    }

    @Nullable
    private static AgentConfiguration resolveAgentConfiguration(BeanContext beanContext,
                                                               String agentName) {
        return beanContext.findBean(AgentConfiguration.class, Qualifiers.byName(agentName)).orElse(null);
    }

    @Nullable
    private static ChatModel resolveChatModel(BeanContext beanContext,
                                              @Nullable String chatModelBeanName) {
        BeanProvider<ChatModel> provider = beanContext.getProvider(ChatModel.class);
        if (chatModelBeanName != null && !chatModelBeanName.isBlank()) {
            return provider.find(Qualifiers.byName(chatModelBeanName))
                .or(() -> provider.find(Qualifiers.byName(NameUtils.camelCase(chatModelBeanName))))
                .orElse(null);
        }
        try {
            return provider.find(null).orElse(null);
        } catch (NonUniqueBeanException _) {
            return null;
        }
    }

    @Nullable
    private static <T> T resolveByNameOrDefault(BeanContext beanContext,
                                                String name,
                                                Class<T> beanType) {
        return resolveByNameOrDefault(beanContext, name, Argument.of(beanType));
    }

    @Nullable
    private static <T> T resolveByNameOrDefault(BeanContext beanContext,
                                                String name,
                                                Argument<T> beanType) {
        Qualifier<T> qualifier = name != null ? Qualifiers.byName(name) : null;
        BeanProvider<T> provider = beanContext.getProvider(beanType);
        try {
            T qualified = provider.find(qualifier).orElse(null);
            return qualified != null ? qualified : provider.find(null).orElse(null);
        } catch (NonUniqueBeanException _) {
            return null;
        }
    }

    private static <T> void lookupByNameOrDefault(BeanContext beanContext,
                                                  String name,
                                                  Class<T> beanType,
                                                  Consumer<T> configurer) {
        T bean = resolveByNameOrDefault(beanContext, name, beanType);
        if (bean != null) {
            configurer.accept(bean);
        }
    }

    // Derive default agent id from interface name, e.g. GreeterAgent -> greeter.
    private static String deriveAgentName(Class<?> type) {
        String simple = NameUtils.getSimpleName(type.getName());
        int dollar = simple.indexOf('$');
        if (dollar >= 0) {
            simple = simple.substring(0, dollar);
        }
        return NameUtils.hyphenate(NameUtils.decapitalizeWithoutSuffix(simple, "Agent"), true);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected AgentBuilder agentBuilder(@Parameter Class<?> agentServiceClass) {
        return AgenticServices.agentBuilder((Class) agentServiceClass);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected SequentialAgentService sequentialAgentService(@Nullable @Parameter Class<?> agentServiceClass) {
        return agentServiceClass == null ? SequentialAgentServiceImpl.builder() : SequentialAgentServiceImpl.builder(agentServiceClass);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ParallelAgentService parallelAgentService(@Nullable @Parameter Class<?> agentServiceClass) {
        return agentServiceClass == null ? ParallelAgentServiceImpl.builder() : ParallelAgentServiceImpl.builder(agentServiceClass);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ParallelMapperService parallelMapperService(@Nullable @Parameter Class<?> agentServiceClass) {
        return agentServiceClass == null ? ParallelMapperServiceImpl.builder() : ParallelMapperServiceImpl.builder(agentServiceClass);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected LoopAgentService loopAgentService(@Nullable @Parameter Class<?> agentServiceClass) {
        return agentServiceClass == null ? LoopAgentServiceImpl.builder() : LoopAgentServiceImpl.builder(agentServiceClass);
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ConditionalAgentService conditionalAgentService(@Nullable @Parameter Class<?> agentServiceClass) {
        return agentServiceClass == null ? ConditionalAgentServiceImpl.builder() : ConditionalAgentServiceImpl.builder(agentServiceClass);
    }

    /**
     * Micronaut-backed workflow builder that routes workflow builder creation through the BeanContext.
     */
    static final class MicronautWorkflowAgentsBuilder implements WorkflowAgentsBuilder {
        private final BeanContext beanContext;

        MicronautWorkflowAgentsBuilder(BeanContext beanContext) {
            this.beanContext = beanContext;
        }

        @SuppressWarnings("unchecked")
        @Override
        public SequentialAgentService<UntypedAgent> sequenceBuilder() {
            return createWorkflowBean(SequentialAgentService.class, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> SequentialAgentService<T> sequenceBuilder(Class<T> agentServiceClass) {
            return createWorkflowBean(SequentialAgentService.class, agentServiceClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public ParallelAgentService<UntypedAgent> parallelBuilder() {
            return createWorkflowBean(ParallelAgentService.class, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> ParallelAgentService<T> parallelBuilder(Class<T> agentServiceClass) {
            return createWorkflowBean(ParallelAgentService.class, agentServiceClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public ParallelMapperService<UntypedAgent> parallelMapperBuilder() {
            return createWorkflowBean(ParallelMapperService.class, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> ParallelMapperService<T> parallelMapperBuilder(Class<T> agentServiceClass) {
            return createWorkflowBean(ParallelMapperService.class, agentServiceClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public LoopAgentService<UntypedAgent> loopBuilder() {
            return createWorkflowBean(LoopAgentService.class, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> LoopAgentService<T> loopBuilder(Class<T> agentServiceClass) {
            return createWorkflowBean(LoopAgentService.class, agentServiceClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public ConditionalAgentService<UntypedAgent> conditionalBuilder() {
            return createWorkflowBean(ConditionalAgentService.class, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> ConditionalAgentService<T> conditionalBuilder(Class<T> agentServiceClass) {
            return createWorkflowBean(ConditionalAgentService.class, agentServiceClass);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private <S> S createWorkflowBean(Class<?> beanType, @Nullable Class<?> agentServiceClass) {
            return (S) beanContext.createBean((Class) beanType, agentServiceClass);
        }
    }
}
