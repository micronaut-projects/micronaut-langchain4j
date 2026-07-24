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
package io.micronaut.langchain4j.agentic.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Named("counting")
@Requires(env = "custom-memory")
final class CountingChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryStore delegate = new InMemoryChatMemoryStore();
    private final AtomicInteger updates = new AtomicInteger();
    private final AtomicBoolean nullMemoryIdSeen = new AtomicBoolean();
    private final Set<Object> memoryIds = ConcurrentHashMap.newKeySet();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        recordMemoryId(memoryId);
        return delegate.getMessages(memoryId);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        recordMemoryId(memoryId);
        updates.incrementAndGet();
        delegate.updateMessages(memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        delegate.deleteMessages(memoryId);
    }

    int getUpdates() {
        return updates.get();
    }

    Set<Object> getMemoryIds() {
        return memoryIds;
    }

    boolean isNullMemoryIdSeen() {
        return nullMemoryIdSeen.get();
    }

    private void recordMemoryId(Object memoryId) {
        if (memoryId == null) {
            nullMemoryIdSeen.set(true);
        } else {
            memoryIds.add(memoryId);
        }
    }
}
