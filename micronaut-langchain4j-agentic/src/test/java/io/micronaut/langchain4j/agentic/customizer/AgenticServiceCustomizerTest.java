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
package io.micronaut.langchain4j.agentic.customizer;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that AgenticServiceCustomizer is applied to both standard agents and supervisor agents.
 */
final class AgenticServiceCustomizerTest {

    @Test
    @DisplayName("Can customize agents using a bean creation listener")
    void customizeAgent() {
        // Configure a chat model so the agent can be built deterministically in 'alternate' environment
        Map<String, Object> props = Map.of(
            "langchain4j.agentic.agents.customized-greeter.chat-model", "friendlyChatModel" // test camelCase resolution
        );
        try (var ctx = ApplicationContext.run(props, "alternate", "customizer")) {
            var tracker = ctx.getBean(TestCustomizerTracker.class);
            tracker.reset();

            var agent = ctx.getBean(CustomizedGreeterAgent.class);
            var out = agent.greet("Bob");
            assertTrue(out.startsWith("[friendly] "), "Expected friendly chat model to be selected");

            assertTrue(tracker.get() >= 1, "Customizer should be invoked at least once");
        }
    }

    @Test
    @DisplayName("Can customize an agent with sub-agents")
    void customizeAgentWithSubAgents() {
        Map<String, Object> props = Map.of(
            "langchain4j.agentic.agents.letter-count.chat-model", "friendlyChatModel",
            "langchain4j.agentic.agents.customized-sequence.chat-model", "friendlyChatModel"
        );
        try (var ctx = ApplicationContext.run(props, "alternate", "customizer")) {
            var tracker = ctx.getBean(TestCustomizerTracker.class);
            tracker.reset();

            var agent = ctx.getBean(CustomizedSequenceAgent.class);
            var result = agent.howManyLetters("simple plan");
            assertEquals(3, tracker.get(), "Customizer should be invoked on the sequence builder and for sub-agents");
        }
    }
}
