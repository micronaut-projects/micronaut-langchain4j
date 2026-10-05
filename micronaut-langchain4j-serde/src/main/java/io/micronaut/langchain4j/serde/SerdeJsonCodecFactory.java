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
package io.micronaut.langchain4j.serde;

import dev.langchain4j.internal.Json;
import dev.langchain4j.spi.PrioritizedFactory;
import dev.langchain4j.spi.json.JsonCodecFactory;
import io.micronaut.core.annotation.Internal;

/**
 * Registers the {@link SerdeJsonCodec} as LangChain4j's general purpose JSON codec ({@code dev.langchain4j.internal.Json}),
 * ahead of other factories.
 *
 * @since 2.4.0
 */
@Internal
public final class SerdeJsonCodecFactory implements JsonCodecFactory, PrioritizedFactory {

    /**
     * The priority of the factory: above the default priority of the factories of other integrations.
     */
    public static final int PRIORITY = 100;

    @Override
    public Json.JsonCodec create() {
        return new SerdeJsonCodec();
    }

    @Override
    public int priority() {
        return PRIORITY;
    }
}
