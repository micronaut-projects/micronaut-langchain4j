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
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.BeanContext;
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
import java.util.Set;

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
     * @param type The type name
     * @param name The name
     * @param toolTypes The tool types
     * @param creationContext The creation context
     * @return The AI services.
     */
    @Bean
    protected AiServices<Object> createAiServices(
        @Parameter Class<Object> type,
        @Parameter @Nullable String name,
        @Parameter @Nullable Set<?> toolTypes,
        @Parameter @Nullable Class<AiServiceCustomizer<Object>> creationContext) {
        AiServices<Object> builder = AiServices
            .builder(type);

        AiServiceCustomizer<Object> creationCustomizer = Optional.ofNullable(creationContext)
            .flatMap(cc -> beanContext.findBean(creationContext))
            .orElseGet(() -> {
                Argument<AiServiceCustomizer> creationContextArgument = Argument.of(AiServiceCustomizer.class, type);
                return beanContext.findBean(creationContextArgument,
                    name != null ? Qualifiers.byName(name) : null
                ).orElse(null);
            });
        List<Object> toolsTyped = toolTypes != null ? toolRegistry.getToolsTyped(toolTypes) : List.of();
        if (CollectionUtils.isNotEmpty(toolsTyped)) {
            builder.tools(toolsTyped);
        }
        beanContext.findBean(
            ChatLanguageModel.class,
            name != null ? Qualifiers.byName(name) : null
        ).ifPresent(builder::chatLanguageModel);

        beanContext.findBean(
            StreamingChatLanguageModel.class,
            name != null ? Qualifiers.byName(name) : null
        ).ifPresent(builder::streamingChatLanguageModel);

        beanContext.findBean(
            ModerationModel.class,
            name != null ? Qualifiers.byName(name) : null
        ).ifPresent(builder::moderationModel);

        builder.chatMemory(
            MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build()
        );

        if (creationCustomizer != null) {
            creationCustomizer.customize(new AiServiceCreationContext<>(
                type,
                name,
                toolsTyped,
                builder
            ));
        }
        return builder;
    }
}
