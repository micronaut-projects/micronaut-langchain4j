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
package io.micronaut.langchain4j.router;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.router.ChatModelRouter;
import dev.langchain4j.model.chat.router.DecisionModelChatModelRouter;
import dev.langchain4j.model.chat.router.RoutingChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.util.Map;

/**
 * Creates the {@link RoutingChatModel} beans configured with {@link RoutingChatModelConfiguration}.
 *
 * @since 2.4.0
 */
@Experimental
@Factory
final class RoutingChatModelFactory {

    private final BeanContext beanContext;

    RoutingChatModelFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @EachBean(RoutingChatModelConfiguration.class)
    RoutingChatModel routingChatModel(RoutingChatModelConfiguration configuration) {
        if (configuration.getRoutes().isEmpty()) {
            throw new ConfigurationException("The routing chat model '" + configuration.getName() + "' has no routes: set "
                + RoutingChatModelConfiguration.PREFIX + "." + configuration.getName() + ".routes");
        }
        RoutingChatModel.Builder builder = RoutingChatModel.builder()
            .router(router(configuration))
            .defaultRoute(configuration.getDefaultRoute());
        for (Map.Entry<String, String> route : configuration.getRoutes().entrySet()) {
            ChatModel chatModel = beanContext.getBean(ChatModel.class, Qualifiers.byName(route.getValue()));
            builder.route(route.getKey(), configuration.getDescriptions().get(route.getKey()), chatModel);
        }
        return builder.build();
    }

    private ChatModelRouter router(RoutingChatModelConfiguration configuration) {
        if (configuration.getRouter() != null) {
            return beanContext.getBean(ChatModelRouter.class, Qualifiers.byName(configuration.getRouter()));
        }
        DecisionModel decisionModel = configuration.getDecisionModel() != null
            ? beanContext.getBean(DecisionModel.class, Qualifiers.byName(configuration.getDecisionModel()))
            : beanContext.getBean(DecisionModel.class);
        DecisionModelChatModelRouter.Builder builder = DecisionModelChatModelRouter.builder().decisionModel(decisionModel);
        if (configuration.getQuestion() != null) {
            builder.question(configuration.getQuestion());
        }
        if (configuration.getMinProbability() != null) {
            builder.minProbability(configuration.getMinProbability());
        }
        if (configuration.getFallbackStrategy() != null) {
            builder.fallbackStrategy(configuration.getFallbackStrategy());
        }
        return builder.build();
    }
}
