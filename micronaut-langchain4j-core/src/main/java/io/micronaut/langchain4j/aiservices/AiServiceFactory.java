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

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.tools.ToolRegistry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An AI services factory.
 */
@Factory
public class AiServiceFactory {

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

        lookupByNameOrDefault(name, ChatLanguageModel.class, builder::chatLanguageModel);

        lookupByNameOrDefault(name, StreamingChatLanguageModel.class, builder::streamingChatLanguageModel);

        lookupByNameOrDefault(name, ModerationModel.class, builder::moderationModel);

        // default to in-memory chat store, but allow replacement
        lookupByNameOrDefault(name, InMemoryChatMemoryStore.class, new InMemoryChatMemoryStore(), (store) -> builder.chatMemory(
            MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryStore(store)
                .build()
        ));

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

        provider.ifPresent(configurer);
        provider.find(qualifier).ifPresent(configurer);
    }
}
