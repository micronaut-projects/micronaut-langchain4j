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
package io.micronaut.langchain4j.agentic.memory;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static io.micronaut.langchain4j.agentic.AgenticServiceFactory.AGENTIC_CONFIG_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that Agentic services receive chat memory from Micronaut's memory provider
 * (MessageWindowChatMemory built from configured ChatMemoryStore) and that message
 * history is passed to the model across turns.
 */
@MicronautTest(rebuildContext = true, environments = "alternate")
class AgenticServiceMemoryIntegrationTest {

    @Test
    @Property(name = AGENTIC_CONFIG_PREFIX + "memoryful.chat-model", value = "memory-aware-chat-model")
    @Property(name = "langchain4j.chat-memory-store.message-window.max-messages", value = "10")
    void conversationHistoryIsPersistedAcrossTurns(MemoryfulAgent agent) {
        assertNotNull(agent);

        var first = agent.say("first shot");
        // We don't assert exact history count for the first call since the agent framework
        // may include system/tool messages; we just ensure it contains the marker.
        assertTrue(first.contains("[mem]"), "First response should come from memory-aware model, got: " + first);

        var second = agent.say("second shot");

        // The memory-aware model encodes previous user message in the response string as:
        // "[mem] history=<n> prevUser=<text>"
        assertTrue(second.contains("[mem]"), "Second response should come from memory-aware model");
        assertTrue(second.contains("prevUser="), "Second response should include previous user message");
        assertTrue(second.contains("first shot"),
            "Second response should reference the first user turn, got: " + second);
    }
}
