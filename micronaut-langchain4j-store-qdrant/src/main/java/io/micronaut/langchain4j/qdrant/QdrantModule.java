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
package io.micronaut.langchain4j.qdrant;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.micronaut.langchain4j.annotation.Lang4jConfig;

@Lang4jConfig(
    models = @Lang4jConfig.Model(
        kind = EmbeddingStore.class,
        impl = QdrantEmbeddingStore.class
    ),
    properties = {
        @Lang4jConfig.Property(name = "client", injected = true),
        @Lang4jConfig.Property(name = "apiKey", common = true),
        @Lang4jConfig.Property(name = "payloadTextKey", common = true, defaultValue = "payload", required = true),
        @Lang4jConfig.Property(name = "collectionName", required = true)
    }
)
final class QdrantModule {
}
