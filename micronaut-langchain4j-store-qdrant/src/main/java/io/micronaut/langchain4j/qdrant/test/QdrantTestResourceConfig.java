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
package io.micronaut.langchain4j.qdrant.test;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.bind.annotation.Bindable;
import io.qdrant.client.grpc.Collections;

/**
 * Test resources configuration for Qdrant.
 * @param dimension The dimension
 * @param distance The distance
 */
@ConfigurationProperties(QdrantTestResourceConfig.PREFIX)
@Internal
record QdrantTestResourceConfig(
    @Bindable(defaultValue = "384")
    Integer dimension,
    @Bindable(defaultValue = "Cosine")
    Collections.Distance distance
) {
    public static final String PREFIX = "test-resources.containers.qdrant";
}
