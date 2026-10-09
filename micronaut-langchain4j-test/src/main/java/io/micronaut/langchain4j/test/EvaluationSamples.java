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

import io.micronaut.core.annotation.Experimental;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads {@link EvaluationSample evaluation samples} from a YAML (or JSON) file of the classpath: a list of samples,
 * or a map with a {@code samples} list.
 *
 * <pre>
 * samples:
 *   - name: framework
 *     input: Which framework do you recommend?
 *     context: Micronaut is a JVM framework for microservices.
 *     expected: Micronaut
 * </pre>
 *
 * @since 2.4.0
 */
@Experimental
public final class EvaluationSamples {

    private EvaluationSamples() {
    }

    /**
     * @param resource The path of the file on the classpath, for example {@code samples/assistant.yml}
     * @return The samples of the file
     */
    public static List<EvaluationSample> load(String resource) {
        String path = resource.startsWith("classpath:") ? resource.substring("classpath:".length()) : resource;
        path = path.startsWith("/") ? path.substring(1) : path;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader() != null
            ? Thread.currentThread().getContextClassLoader()
            : EvaluationSamples.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalArgumentException("Evaluation samples not found on the classpath: " + resource);
            }
            return samples(resource, new Yaml(new SafeConstructor(new LoaderOptions())).load(input));
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading the evaluation samples " + resource, e);
        }
    }

    private static List<EvaluationSample> samples(String resource, Object document) {
        Object samples = document instanceof Map<?, ?> map ? map.get("samples") : document;
        if (!(samples instanceof List<?> list)) {
            throw new IllegalArgumentException("The evaluation samples " + resource + " must be a list, or a map with a 'samples' list");
        }
        List<EvaluationSample> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> sample) || !(sample.get("input") instanceof String input)) {
                throw new IllegalArgumentException("The evaluation sample " + (i + 1) + " of " + resource + " has no input");
            }
            Object name = sample.get("name");
            result.add(new EvaluationSample(
                name != null ? name.toString() : "sample " + (i + 1),
                input,
                text(sample.get("context")),
                text(sample.get("expected"))
            ));
        }
        return result;
    }

    private static @Nullable String text(@Nullable Object value) {
        return value != null ? value.toString() : null;
    }
}
