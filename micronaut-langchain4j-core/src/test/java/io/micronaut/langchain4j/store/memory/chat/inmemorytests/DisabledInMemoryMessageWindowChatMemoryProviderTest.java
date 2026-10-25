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
package io.micronaut.langchain4j.store.memory.chat.inmemorytests;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Property(name = "langchain4j.chat-memory-store.inmemory.enabled", value = StringUtils.FALSE)
@Property(name = "spec.name", value = "DisabledInMemoryMessageWindowChatMemoryProviderTest")
@MicronautTest(startApplication = false)
class DisabledInMemoryMessageWindowChatMemoryProviderTest {

    @Inject
    BeanContext beanContext;

    @Test
    void disablingInMemoryChatMemoryRemovesProviderAndStoreBeans() {
        assertFalse(beanContext.containsBean(ChatMemoryProvider.class, Qualifiers.byName("InMemoryMessageWindow")));
        assertFalse(beanContext.containsBean(ChatMemoryStore.class, Qualifiers.byName("inMemory")));
    }
}
