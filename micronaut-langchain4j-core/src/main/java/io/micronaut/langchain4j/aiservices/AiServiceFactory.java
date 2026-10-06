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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.search.ToolSearchStrategy;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.services.PublisherAdapter;
import dev.langchain4j.spi.services.TokenStreamAdapter;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import org.jspecify.annotations.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.jsonschema.StructuredOutputSchemas;
import io.micronaut.langchain4j.tools.ToolRegistry;
import io.micronaut.langchain4j.utils.RetrievalUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An AI services factory.
 */
@Factory
public class AiServiceFactory {
    private static final Collection<TokenStreamAdapter> TOKEN_STREAM_ADAPTERS = ServiceHelper.loadFactories(TokenStreamAdapter.class);
    private static final Collection<PublisherAdapter> PUBLISHER_ADAPTERS = ServiceHelper.loadFactories(PublisherAdapter.class);

    private final BeanContext beanContext;
    private final ToolRegistry toolRegistry;

    public AiServiceFactory(BeanContext beanContext, ToolRegistry toolRegistry) {
        this.beanContext = beanContext;
        this.toolRegistry = toolRegistry;
    }

    /**
     * Creates instances of {@link AiServices}.
     *
     * <p>A {@link io.micronaut.context.event.BeanCreatedEventListener} can be registered to intercept creation.</p>
     *
     * <p>Retrieval is configured only when a {@link dev.langchain4j.rag.RetrievalAugmentor} bean or a
     * {@link dev.langchain4j.rag.content.retriever.ContentRetriever} bean exists, see {@link RetrievalUtils}.</p>
     *
     * <p>The methods returning a type with a generated JSON schema send it as the response format, see
     * {@link StructuredOutputSchemas}.</p>
     *
     * @param serviceDef The service definition
     * @return The AI services.
     */
    @Bean
    protected AiServices<Object> createAiServices(
        @Parameter AiServiceDef<Object> serviceDef) {
        Class<Object> type = serviceDef.type();
        String name = serviceDef.name();

        MicronautAiServiceContext context = new MicronautAiServiceContext(type, serviceDef.beanDefinition(), beanContext);
        AiServices<Object> builder = AiServices.builder(context);

        AiServiceCustomizer<Object> creationCustomizer = Optional.ofNullable(serviceDef.customizer())
            .flatMap(beanContext::findBean)
            .orElseGet(() -> {
                AtomicReference<AiServiceCustomizer<Object>> ref = new AtomicReference<>();
                lookupByNameOrDefault(name, Argument.of(AiServiceCustomizer.class, type), null, ref::set);
                return ref.get();
            });

        // the tools invoke the @Tool methods through their ExecutableMethod: LangChain4j does not scan the tool classes
        Set<Class<?>> toolTypes = serviceDef.tools();
        List<AiServiceTool> toolsTyped = toolTypes != null ? toolRegistry.getAiServiceTools(toolTypes) : List.of();
        if (CollectionUtils.isNotEmpty(toolsTyped)) {
            builder.tools(toolsTyped);
        }
        configureToolProviders(serviceDef, builder);
        lookupByNameOrDefault(name, ToolExecutionErrorHandler.class, builder::toolExecutionErrorHandler);
        lookupByNameOrDefault(name, ToolArgumentsErrorHandler.class, builder::toolArgumentsErrorHandler);
        lookupByNameOrDefault(name, ToolSearchStrategy.class, builder::toolSearchStrategy);
        configureToolCalling(name, builder);

        // AiServiceListener beans observe every AI service, Micronaut event listeners receive the same events
        builder.registerListeners(beanContext.getBeansOfType(AiServiceListener.class).stream()
            .<AiServiceListener<?>>map(listener -> listener)
            .toList());
        builder.registerListeners(AiServiceEventPublishers.create(beanContext));

        ModelSelection modelSelection = selectModels(serviceDef.beanDefinition());
        if (modelSelection.chatModel()) {
            lookupByNameOrDefault(name, ChatModel.class, builder::chatModel);
        }
        if (modelSelection.streamingChatModel()) {
            lookupByNameOrDefault(name, StreamingChatModel.class, builder::streamingChatModel);
        }

        lookupByNameOrDefault(name, ModerationModel.class, builder::moderationModel);

        lookupByNameOrDefault(name, ChatMemoryProvider.class, builder::chatMemoryProvider);

        RetrievalUtils.configureRetrieval(beanContext, name, builder::retrievalAugmentor, builder::contentRetriever);
        if (creationCustomizer != null) {
            creationCustomizer.customize(new AiServiceCreationContext<>(
                serviceDef,
                builder
            ));
        }
        // after the customizer, which may replace the chat model or the chat request transformer
        beanContext.findBean(StructuredOutputSchemas.class)
            .ifPresent(schemas -> schemas.configure(context, returnTypes(serviceDef.beanDefinition())));
        return builder;
    }

    private void configureToolProviders(AiServiceDef<Object> serviceDef, AiServices<Object> builder) {
        List<String> names = serviceDef.toolProviders();
        List<String> mcpClients = serviceDef.mcpClients();
        List<ToolProvider> toolProviders = new ArrayList<>();
        if (names != null) {
            names.stream()
                .map(toolProviderName -> beanContext.getBean(ToolProvider.class, Qualifiers.byName(toolProviderName)))
                .forEach(toolProviders::add);
        } else if (mcpClients == null) {
            // like the other components: the provider named after the service, otherwise the default one
            String name = serviceDef.name();
            Optional<ToolProvider> toolProvider = name != null ? beanContext.findBean(ToolProvider.class, Qualifiers.byName(name)) : Optional.empty();
            toolProvider.or(this::defaultToolProvider).ifPresent(toolProviders::add);
        }
        if (CollectionUtils.isNotEmpty(mcpClients)) {
            toolProviders.add(McpToolProviders.create(beanContext, serviceDef.type(), mcpClients));
        }
        if (!toolProviders.isEmpty()) {
            builder.toolProviders(toolProviders);
        }
    }

    /**
     * The default tool provider is the only unqualified (or {@code @Primary}) {@link ToolProvider} bean. Providers
     * qualified with a name are only used by the services that select them, and several unqualified providers are
     * ambiguous, so none is used rather than exposing tools to a service by accident.
     */
    private Optional<ToolProvider> defaultToolProvider() {
        List<BeanDefinition<ToolProvider>> candidates = beanContext.getBeanDefinitions(ToolProvider.class).stream()
            .filter(definition -> definition.isPrimary() || definition.getDeclaredQualifier() == null)
            .toList();
        return candidates.size() == 1 ? Optional.of(beanContext.getBean(candidates.getFirst())) : Optional.empty();
    }

    private void configureToolCalling(@Nullable String name, AiServices<Object> builder) {
        AiServiceConfiguration configuration = Optional.ofNullable(name)
            .flatMap(n -> beanContext.findBean(AiServiceConfiguration.class, Qualifiers.byName(n)))
            .or(() -> beanContext.findBean(AiServiceConfiguration.class, Qualifiers.byName(AiServiceConfiguration.DEFAULT)))
            .orElse(null);
        if (configuration == null) {
            return;
        }
        if (configuration.getMaxToolCallingRoundTrips() != null) {
            builder.maxToolCallingRoundTrips(configuration.getMaxToolCallingRoundTrips());
        }
        if (configuration.isExecuteToolsConcurrently()) {
            String executorName = configuration.getToolExecutor();
            if (executorName != null) {
                builder.executeToolsConcurrently(beanContext.getBean(ExecutorService.class, Qualifiers.byName(executorName)));
            } else {
                builder.executeToolsConcurrently();
            }
        }
    }

    private static List<Argument<?>> returnTypes(BeanDefinition<?> beanDefinition) {
        return beanDefinition.getExecutableMethods().stream()
            .filter(method -> method.getDeclaringType() != Object.class)
            .<Argument<?>>map(method -> method.getReturnType().asArgument())
            .toList();
    }

    private <T> void lookupByNameOrDefault(String name, Class<T> beanType, Consumer<T> configurer) {
        lookupByNameOrDefault(name, Argument.of(beanType), null, configurer);
    }

    private <T> void lookupByNameOrDefault(String name, Argument<T> beanType, @Nullable T defaultValue, Consumer<T> configurer) {
        Qualifier<T> qualifier = name != null ? Qualifiers.byName(name) : null;
        BeanProvider<T> provider = beanContext.getProvider(
            beanType
        );

        if (defaultValue != null) {
            configurer.accept(defaultValue);
        }

        provider.find(qualifier).ifPresentOrElse(configurer,
            () -> provider.ifPresent(configurer));
    }

    static ModelSelection selectModels(BeanDefinition<?> beanDefinition) {
        boolean hasStreamingMethods = false;
        boolean hasNonStreamingMethods = false;
        for (ExecutableMethod<?, ?> method : beanDefinition.getExecutableMethods()) {
            if (method.getDeclaringType() == Object.class) {
                continue;
            }
            if (isStreamingReturnType(method)) {
                hasStreamingMethods = true;
            } else {
                hasNonStreamingMethods = true;
            }
        }
        if (!hasStreamingMethods) {
            return ModelSelection.CHAT;
        }
        if (!hasNonStreamingMethods) {
            return ModelSelection.STREAMING;
        }
        return ModelSelection.BOTH;
    }

    private static boolean isStreamingReturnType(ExecutableMethod<?, ?> method) {
        Argument<?> returnType = method.getReturnType().asArgument();
        // TokenStream and the reactive types (Flow.Publisher, Flux, Publisher...) stream the response of a streaming model
        if (TokenStream.class.isAssignableFrom(returnType.getType()) || Flow.Publisher.class.isAssignableFrom(returnType.getType())) {
            return true;
        }
        for (PublisherAdapter publisherAdapter : PUBLISHER_ADAPTERS) {
            if (publisherAdapter.canAdapt(returnType.asType())) {
                return true;
            }
        }
        for (TokenStreamAdapter tokenStreamAdapter : TOKEN_STREAM_ADAPTERS) {
            if (tokenStreamAdapter.canAdaptTokenStreamTo(returnType.asType())) {
                return true;
            }
        }
        return false;
    }

    enum ModelSelection {
        CHAT(true, false),
        STREAMING(false, true),
        BOTH(true, true);

        private final boolean chatModel;
        private final boolean streamingChatModel;

        ModelSelection(boolean chatModel, boolean streamingChatModel) {
            this.chatModel = chatModel;
            this.streamingChatModel = streamingChatModel;
        }

        boolean chatModel() {
            return chatModel;
        }

        boolean streamingChatModel() {
            return streamingChatModel;
        }
    }
}
