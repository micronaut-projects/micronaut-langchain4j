/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.langchain4j.redis.memory;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
@Internal
class RedisChatMemoryStoreFactory {
    @Named("redis")
    @Prototype
    RedisChatMemoryStore.Builder createRedisChatMemoryStoreBuilder(RedisChatMemoryStoreConfiguration config) {
        return config.getBuilder();
    }

    @Singleton
    @EachBean(RedisChatMemoryStore.Builder.class)
    RedisChatMemoryStore createRedisChatMemoryStore(RedisChatMemoryStore.Builder builder) {
        return builder.build();
    }
}
