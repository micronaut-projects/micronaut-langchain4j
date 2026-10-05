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
package dev.langchain4j.internal;

import io.micronaut.core.annotation.Internal;

/**
 * Gives micronaut-langchain4j-serde access to LangChain4j's default, package-private Jackson codec, used for the types
 * Micronaut Serialization cannot map. Declared in LangChain4j's package so that the codec is created without
 * reflection, and so that a change of the codec breaks the compilation rather than the application.
 *
 * @since 2.4.0
 */
@Internal
public final class MicronautJacksonJsonCodecs {

    private MicronautJacksonJsonCodecs() {
    }

    /**
     * @return A new instance of LangChain4j's default Jackson codec
     */
    public static Json.JsonCodec jackson() {
        return new JacksonJsonCodec();
    }
}
