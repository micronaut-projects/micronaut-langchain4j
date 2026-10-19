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

import io.micronaut.core.annotation.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sends the image at the URL of a parameter of an {@link AiService} method to the model, as a
 * {@link dev.langchain4j.data.message.ImageContent}, together with the user message.
 *
 * <p>The parameter is a {@code String}, a {@link java.net.URI} or a {@link java.net.URL}, or a collection of them to
 * send several.</p>
 *
 * <p>Like any content argument of a LangChain4j AI service, the parameter must also be annotated with
 * {@link dev.langchain4j.service.UserMessage}: the content is added to the user message, after its text.</p>
 *
 * @since 2.4.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Experimental
public @interface ImageUrl {
}
