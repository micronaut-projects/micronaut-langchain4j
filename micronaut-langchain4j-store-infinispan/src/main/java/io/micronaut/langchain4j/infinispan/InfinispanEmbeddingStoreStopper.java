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

import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import io.micronaut.context.event.BeanPreDestroyEvent;
import io.micronaut.context.event.BeanPreDestroyEventListener;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;

/**
 * Stops the Hot Rod client of an Infinispan embedding store with the store. The store creates its own
 * {@link org.infinispan.client.hotrod.RemoteCacheManager} and is not closeable, so nothing else stops the connections
 * and threads of the client when the application context is closed.
 */
@Internal
@Singleton
final class InfinispanEmbeddingStoreStopper implements BeanPreDestroyEventListener<InfinispanEmbeddingStore> {

    @Override
    public InfinispanEmbeddingStore onPreDestroy(BeanPreDestroyEvent<InfinispanEmbeddingStore> event) {
        InfinispanEmbeddingStore store = event.getBean();
        store.remoteCache().getRemoteCacheContainer().stop();
        return store;
    }
}
