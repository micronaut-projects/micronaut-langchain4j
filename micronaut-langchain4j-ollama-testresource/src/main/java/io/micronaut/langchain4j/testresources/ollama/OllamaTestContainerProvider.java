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
package io.micronaut.langchain4j.testresources.ollama;

import io.micronaut.testresources.testcontainers.AbstractTestContainersProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

public class OllamaTestContainerProvider
    extends AbstractTestContainersProvider<OllamaContainer> {
    public static final String MODEL_NAME = "model-name";
    public static final String BASE_URL = "base-url";
    private static final String PREFIX = "langchain4j.ollama.chat-models";
    private static final List<String> SUPPORTED_LIST = List.of(BASE_URL);

    @Override
    protected String getSimpleName() {
        return "ollama";
    }

    @Override
    protected String getDefaultImageName() {
        return "langchain4j/ollama-orca-mini:latest";
    }

    @Override
    protected OllamaContainer createContainer(DockerImageName imageName, Map<String, Object> requestedProperties, Map<String, Object> testResourcesConfig) {
        return new OllamaContainer(imageName.asCompatibleSubstituteFor("ollama/ollama"));
    }

    @Override
    public List<String> getRequiredPropertyEntries() {
        return List.of(PREFIX);
    }

    @Override
    protected Optional<String> resolveProperty(String propertyName, OllamaContainer container) {
        String prop = languageModelPropertyFrom(propertyName);
        if (BASE_URL.equals(prop)) {
            return Optional.ofNullable(container.getEndpoint());
        }
        return Optional.empty();
    }

    @Override
    public List<String> getResolvableProperties(Map<String, Collection<String>> propertyEntries, Map<String, Object> testResourcesConfig) {
        Collection<String> chatModels = propertyEntries.getOrDefault(PREFIX, Collections.emptyList());
        return chatModels.stream()
            .flatMap(chatModel -> SUPPORTED_LIST.stream().map(p -> PREFIX + "." + chatModel + "." + p))
            .toList();
    }

    @Override
    protected boolean shouldAnswer(String propertyName, Map<String, Object> requestedProperties, Map<String, Object> testResourcesConfig) {
        return isOllamaProperty(propertyName);
    }

    @Override
    public List<String> getRequiredProperties(String expression) {
        if (isOllamaProperty(expression)) {
            String datasource = languageModelNameFrom(expression);
            return Stream.of(
                    languageModelExpressionOf(datasource, MODEL_NAME)
            ).toList();
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isOllamaProperty(String expression) {
        return expression.startsWith(PREFIX);
    }

    protected static String languageModelNameFrom(String expression) {
        expression = expression.substring(PREFIX.length() + 1);
        return expression.substring(0, expression.indexOf('.'));
    }

    protected static String languageModelExpressionOf(String languageModel, String property) {
        return PREFIX + "." + languageModel + "." + property;
    }

    protected static String languageModelPropertyFrom(String expression) {
        expression = expression.substring(PREFIX.length() + 1);
        return expression.substring(1 + expression.indexOf('.'));
    }
}
