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
package io.micronaut.langchain4j.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Generates the code necessary to integrate a ChatLanguageModel provider.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Lang4jConfig {

    /**
     * Configuration for different properties.
     * @return The properties
     */
    Property[] properties() default {};

    /**
     * Common properties to all configuration classes that are required.
     * @return The required common
     */
    Model[] models();

    /**
     * Configuration for an individual property.
     */
    @interface Property {
        /**
         * @return The property name.
         */
        String name();

        /**
         * @return The default value if any.
         */
        String defaultValue() default "";

        /**
         * Is it required.
         * @return True if required
         */
        boolean required() default false;

        /**
         * Is it injected.
         * @return True if injected
         */
        boolean injected() default false;

        /**
         * Is it common to all models.
         * @return True if common.
         */
        boolean common() default false;
    }

    /**
     * Add configuration for the given model.
     */
    @interface Model {
        /**
         * The kind of model.
         * @return The model kind.
         */
        Class<?> kind();

        /**
         * The implementation of the model.
         * @return The implementation of the model.
         */
        Class<?> impl();
    }
}
