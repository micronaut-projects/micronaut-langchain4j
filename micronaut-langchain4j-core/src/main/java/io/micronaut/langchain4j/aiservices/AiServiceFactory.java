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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.services.TokenStreamAdapter;
import dev.langchain4j.store.embedding.EmbeddingStore;
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
import io.micronaut.langchain4j.tools.ToolRegistry;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An AI services factory.
 */
@Factory
public class AiServiceFactory {
    private static final Collection<TokenStreamAdapter> TOKEN_STREAM_ADAPTERS = ServiceHelper.loadFactories(TokenStreamAdapter.class);

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
     * @param serviceDef The service definition
     * @return The AI services.
     */
    @Bean
    protected AiServices<Object> createAiServices(
        @Parameter AiServiceDef<Object> serviceDef) {
        Class<Object> type = serviceDef.type();
        String name = serviceDef.name();

        AiServices<Object> builder = AiServices
            .builder(type);

        AiServiceCustomizer<Object> creationCustomizer = Optional.ofNullable(serviceDef.customizer())
            .flatMap(beanContext::findBean)
            .orElseGet(() -> {
                AtomicReference<AiServiceCustomizer<Object>> ref = new AtomicReference<>();
                lookupByNameOrDefault(name, Argument.of(AiServiceCustomizer.class, type), null, ref::set);
                return ref.get();
            });

        List<Object> toolsTyped = serviceDef.tools() != null ? toolRegistry.getToolsTyped(serviceDef.tools()) : List.of();
        if (CollectionUtils.isNotEmpty(toolsTyped)) {
            builder.tools(toolsTyped);
        }

        ModelSelection modelSelection = selectModels(serviceDef.beanDefinition());
        if (modelSelection.chatModel()) {
            lookupByNameOrDefault(name, ChatModel.class, builder::chatModel);
        }
        if (modelSelection.streamingChatModel()) {
            lookupByNameOrDefault(name, StreamingChatModel.class, builder::streamingChatModel);
        }

        lookupByNameOrDefault(name, ModerationModel.class, builder::moderationModel);

        lookupByNameOrDefault(name, ChatMemoryProvider.class, builder::chatMemoryProvider);

        lookupByNameOrDefault(name, EmbeddingModel.class, null, embeddingModel ->
            lookupByNameOrDefault(name, EmbeddingStore.class, null, embeddingStore ->
                builder.contentRetriever(new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel))));
        if (creationCustomizer != null) {
            creationCustomizer.customize(new AiServiceCreationContext<>(
                serviceDef,
                builder
            ));
        }
        return builder;
    }

    private <T> void lookupByNameOrDefault(String name, Class<T> beanType, @Nullable T defaultValue, Consumer<T> configurer) {
        lookupByNameOrDefault(name, Argument.of(beanType), defaultValue, configurer);
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
        if (TokenStream.class.isAssignableFrom(returnType.getType())) {
            return true;
        }
        for (TokenStreamAdapter tokenStreamAdapter : TOKEN_STREAM_ADAPTERS) {
            if (tokenStreamAdapter.canAdaptTokenStreamTo(returnType.getType())) {
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
