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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.langchain4j.annotation.AiService;

/**
 * Interface that allows customization the creation of {@link dev.langchain4j.service.AiServices}.
 *
 * @param <T> The type of the service
 * @see AiService
 */
@FunctionalInterface
public interface AiServiceCustomizer<T> {

    /**
     * Call back invocation that receives the {@link AiServiceCreationContext}.
     *
     * <p>The context allows customizing the builder and analysing the metadata associated with the service.</p>
     * @param creationContext The creation context, never {@code null}
     */
    void customize(@NonNull AiServiceCreationContext<T> creationContext);
}
