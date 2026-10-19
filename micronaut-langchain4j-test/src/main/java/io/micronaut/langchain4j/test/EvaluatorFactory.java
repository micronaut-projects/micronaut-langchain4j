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
package io.micronaut.langchain4j.test;

import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.evaluation.FactCheckingEvaluator;
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator;
import jakarta.inject.Singleton;

/**
 * Exposes the evaluators as beans that tests can inject, judged by the {@code ChatModel} bean named by
 * {@code langchain4j.evaluation.chat-model}, otherwise by the default {@code ChatModel} bean.
 *
 * @since 2.4.0
 */
@Experimental
@Factory
@Requires(beans = ChatModel.class)
final class EvaluatorFactory {

    private final ChatModel judge;

    EvaluatorFactory(BeanContext beanContext, @Value("${langchain4j.evaluation.chat-model:}") String chatModel) {
        this.judge = StringUtils.isNotEmpty(chatModel)
            ? beanContext.getBean(ChatModel.class, Qualifiers.byName(chatModel))
            : beanContext.getBean(ChatModel.class);
    }

    @Singleton
    RelevancyEvaluator relevancyEvaluator() {
        return new RelevancyEvaluator(judge);
    }

    @Singleton
    FactCheckingEvaluator factCheckingEvaluator() {
        return new FactCheckingEvaluator(judge);
    }
}
