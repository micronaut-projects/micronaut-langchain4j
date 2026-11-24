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
package io.micronaut.langchain4j.infinispan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanStoreConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.jupiter.api.Test;

class InfinispanEmbeddingStoreStopperTest {

    private static final AtomicInteger STOPPED = new AtomicInteger();

    @Test
    void closingTheContextStopsTheClientOfTheStore() {
        STOPPED.set(0);
        try (ApplicationContext context = ApplicationContext.builder()
            .deduceEnvironment(false)
            .properties(Map.of("spec.name", "InfinispanEmbeddingStoreStopperTest"))
            .start()) {
            context.getBean(EmbeddingStore.class);
            assertEquals(0, STOPPED.get());
        }
        assertEquals(1, STOPPED.get());
    }

    @Factory
    @Requires(property = "spec.name", value = "InfinispanEmbeddingStoreStopperTest")
    static class Stores {

        // exposed like the generated factory exposes the store
        @Context
        @Bean(typed = EmbeddingStore.class)
        InfinispanEmbeddingStore store() {
            return new InfinispanEmbeddingStore(new CountingCacheManager(), new InfinispanStoreConfiguration(
                "test", 3, null, null, null, null, null, null, null, false, false));
        }
    }

    private static final class CountingCacheManager extends RemoteCacheManager {

        CountingCacheManager() {
            super(false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> RemoteCache<K, V> getCache(String cacheName) {
            return (RemoteCache<K, V>) Proxy.newProxyInstance(
                RemoteCache.class.getClassLoader(),
                new Class<?>[] {RemoteCache.class},
                (proxy, method, args) -> method.getName().equals("getRemoteCacheContainer") ? this : null);
        }

        @Override
        public void stop() {
            STOPPED.incrementAndGet();
        }
    }
}
