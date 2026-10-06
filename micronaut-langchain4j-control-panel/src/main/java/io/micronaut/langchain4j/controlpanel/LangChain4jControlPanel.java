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
package io.micronaut.langchain4j.controlpanel;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.controlpanel.core.AbstractControlPanel;
import io.micronaut.controlpanel.core.config.ControlPanelConfiguration;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.tools.ToolRegistry;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A control panel that lists the AI models, AI services and tools of the application, and lets the user chat with the
 * chat models.
 */
@Singleton
@Internal
@ReflectiveAccess
@Requires(property = LangChain4jControlPanel.ENABLED_PROPERTY, value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
public final class LangChain4jControlPanel extends AbstractControlPanel<LangChain4jControlPanel.Body> {

    /**
     * The name of the control panel.
     */
    public static final String NAME = "langchain4j";

    /**
     * The property that enables the control panel.
     */
    public static final String ENABLED_PROPERTY = ControlPanelConfiguration.PREFIX + "." + NAME + ".enabled";

    private static final Category CATEGORY = new Category("ai", "AI", "fas fa-robot", 50);

    private final BeanContext beanContext;
    private final ToolRegistry toolRegistry;

    LangChain4jControlPanel(@Named(NAME) ControlPanelConfiguration configuration, BeanContext beanContext, ToolRegistry toolRegistry) {
        super(NAME, configuration);
        this.beanContext = beanContext;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Body getBody() {
        return new Body(
            models(ChatModel.class),
            models(EmbeddingModel.class),
            models(ImageModel.class),
            models(ScoringModel.class),
            models(EmbeddingStore.class),
            beanContext.getBeanDefinitions(Qualifiers.byStereotype(AiService.class)).stream()
                .map(definition -> aiServiceInterface(definition.getBeanType()).getName())
                .sorted()
                .toList(),
            toolRegistry.getAllAiServiceTools().stream()
                .map(AiServiceTool::toolSpecification)
                .map(specification -> new Tool(specification.name(), description(specification)))
                .sorted(Comparator.comparing(Tool::name))
                .toList()
        );
    }

    @Override
    public String getBadge() {
        return String.valueOf(beanContext.getBeanDefinitions(ChatModel.class).size());
    }

    @Override
    public Category getCategory() {
        return CATEGORY;
    }

    @Override
    public View getBodyView() {
        return new View("/views/" + NAME + "/body");
    }

    @Override
    public View getDetailedView() {
        return new View("/views/" + NAME + "/detail");
    }

    private <T> List<Model> models(Class<T> type) {
        return beanContext.getBeanRegistrations(type).stream()
            .map(registration -> new Model(name(registration), registration.getBean().getClass().getName()))
            .sorted(Comparator.comparing(Model::name))
            .toList();
    }

    /**
     * The bean type of an AI service is the implementation generated for its interface.
     */
    private static Class<?> aiServiceInterface(Class<?> beanType) {
        if (beanType.isInterface()) {
            return beanType;
        }
        return Arrays.stream(beanType.getInterfaces())
            .filter(type -> !type.getName().startsWith("io.micronaut.aop."))
            .findFirst()
            .orElse(beanType);
    }

    static String name(BeanRegistration<?> registration) {
        return registration.getBeanDefinition().stringValue(Named.class).orElse("default");
    }

    private static String description(ToolSpecification specification) {
        return specification.description() == null ? "" : specification.description();
    }

    /**
     * The body of the control panel.
     *
     * @param chatModels The chat models
     * @param embeddingModels The embedding models
     * @param imageModels The image models
     * @param scoringModels The scoring models
     * @param embeddingStores The embedding stores
     * @param aiServices The AI service interfaces
     * @param tools The tools
     */
    @ReflectiveAccess
    public record Body(List<Model> chatModels,
                       List<Model> embeddingModels,
                       List<Model> imageModels,
                       List<Model> scoringModels,
                       List<Model> embeddingStores,
                       List<String> aiServices,
                       List<Tool> tools) {
    }

    /**
     * A model or embedding store bean.
     *
     * @param name The bean name
     * @param type The implementation type
     */
    @ReflectiveAccess
    public record Model(String name, String type) {
    }

    /**
     * A tool.
     *
     * @param name The tool name
     * @param description The tool description
     */
    @ReflectiveAccess
    public record Tool(String name, String description) {
    }
}
