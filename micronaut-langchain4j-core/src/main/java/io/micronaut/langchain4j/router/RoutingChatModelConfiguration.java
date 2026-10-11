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

import dev.langchain4j.model.chat.router.DecisionModelChatModelRouter;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Experimental;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configures a {@link dev.langchain4j.model.chat.router.RoutingChatModel}: a {@code ChatModel} bean that sends each
 * request to one of several chat models, chosen by a {@link dev.langchain4j.model.chat.router.ChatModelRouter}.
 *
 * <pre>
 * langchain4j.routing-chat-models.assistant.routes.fast=small
 * langchain4j.routing-chat-models.assistant.routes.expert=large
 * langchain4j.routing-chat-models.assistant.descriptions.fast=Greetings and simple questions
 * langchain4j.routing-chat-models.assistant.descriptions.expert=Reasoning, maths and code
 * langchain4j.routing-chat-models.assistant.default-route=fast
 * </pre>
 *
 * @since 2.4.0
 */
@Experimental
@EachProperty(RoutingChatModelConfiguration.PREFIX)
public final class RoutingChatModelConfiguration {

    /**
     * The prefix of the routing chat models.
     */
    public static final String PREFIX = "langchain4j.routing-chat-models";

    private final String name;
    private Map<String, String> routes = new LinkedHashMap<>();
    private Map<String, String> descriptions = new LinkedHashMap<>();
    private @Nullable String defaultRoute;
    private @Nullable String router;
    private @Nullable String decisionModel;
    private @Nullable String question;
    private @Nullable Double minProbability;
    private DecisionModelChatModelRouter.@Nullable FallbackStrategy fallbackStrategy;

    /**
     * @param name The name of the routing chat model bean
     */
    public RoutingChatModelConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the routing chat model bean
     */
    public String getName() {
        return name;
    }

    /**
     * @return The names of the {@code ChatModel} beans, by route
     */
    public Map<String, String> getRoutes() {
        return routes;
    }

    /**
     * The names of the {@code ChatModel} beans, by route.
     *
     * @param routes The routes
     */
    public void setRoutes(Map<String, String> routes) {
        this.routes = routes;
    }

    /**
     * @return The descriptions of the requests that each route handles
     */
    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    /**
     * The descriptions of the requests that each route handles, which the router compares the requests with.
     *
     * @param descriptions The descriptions, by route
     */
    public void setDescriptions(Map<String, String> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * @return The route used when the router does not choose one
     */
    public @Nullable String getDefaultRoute() {
        return defaultRoute;
    }

    /**
     * The route used when the router does not choose one.
     *
     * @param defaultRoute The default route
     */
    public void setDefaultRoute(@Nullable String defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    /**
     * @return The name of the {@code ChatModelRouter} bean
     */
    public @Nullable String getRouter() {
        return router;
    }

    /**
     * The name of the {@code ChatModelRouter} bean that chooses the route. By default, a router asks the
     * {@code DecisionModel} (see {@link #setDecisionModel(String)}) which route matches the request.
     *
     * @param router The name of the router bean
     */
    public void setRouter(@Nullable String router) {
        this.router = router;
    }

    /**
     * @return The name of the {@code DecisionModel} bean of the default router
     */
    public @Nullable String getDecisionModel() {
        return decisionModel;
    }

    /**
     * The name of the {@code DecisionModel} bean of the default router, otherwise the default {@code DecisionModel}.
     *
     * @param decisionModel The name of the decision model bean
     */
    public void setDecisionModel(@Nullable String decisionModel) {
        this.decisionModel = decisionModel;
    }

    /**
     * @return The question the default router asks the decision model
     */
    public @Nullable String getQuestion() {
        return question;
    }

    /**
     * The question the default router asks the decision model.
     *
     * @param question The question
     */
    public void setQuestion(@Nullable String question) {
        this.question = question;
    }

    /**
     * @return The minimum probability of the route chosen by the default router
     */
    public @Nullable Double getMinProbability() {
        return minProbability;
    }

    /**
     * The minimum probability of the route chosen by the default router, below which the fallback strategy applies.
     *
     * @param minProbability The minimum probability
     */
    public void setMinProbability(@Nullable Double minProbability) {
        this.minProbability = minProbability;
    }

    /**
     * @return What the default router does when no route is likely enough
     */
    public DecisionModelChatModelRouter.@Nullable FallbackStrategy getFallbackStrategy() {
        return fallbackStrategy;
    }

    /**
     * What the default router does when no route is likely enough.
     *
     * @param fallbackStrategy The fallback strategy
     */
    public void setFallbackStrategy(DecisionModelChatModelRouter.@Nullable FallbackStrategy fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }
}
