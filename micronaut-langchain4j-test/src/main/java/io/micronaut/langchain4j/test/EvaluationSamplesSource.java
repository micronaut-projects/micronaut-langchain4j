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
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides the {@link EvaluationSample evaluation samples} of a classpath file to a JUnit
 * {@link org.junit.jupiter.params.ParameterizedTest}, one invocation per sample.
 *
 * @since 2.4.0
 */
@Experimental
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@ArgumentsSource(EvaluationSamplesArgumentsProvider.class)
public @interface EvaluationSamplesSource {

    /**
     * @return The path of the sample file on the classpath, for example {@code samples/assistant.yml}
     */
    String value();
}
