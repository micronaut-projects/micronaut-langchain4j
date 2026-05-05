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
package io.micronaut.langchain4j.agentic.configuration;

import io.micronaut.langchain4j.agentic.shared.agent.CreativeWriterAgent;
import io.micronaut.langchain4j.agentic.shared.agent.GreeterAgent;
import io.micronaut.langchain4j.agentic.shared.agent.SupervisorAgent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static io.micronaut.langchain4j.agentic.AgenticServiceFactory.AGENTIC_CONFIG_PREFIX;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test demonstrating configuration properties for agentic services.
 * This test proves that configuration properties actually affect agent behavior.
 */
@MicronautTest(rebuildContext = true, environments = "alternate")
class AgenticServiceConfigurationTest {

    @Test
    @Property(name = AGENTIC_CONFIG_PREFIX + "greeter.chat-model", value = "friendly-chat-model")
    void demonstratesFriendlyChatModelConfiguration(GreeterAgent greeterAgent) {
        var answer = greeterAgent.greet("Alex");
        assertTrue(answer.startsWith("[friendly]"), "Expected friendly model prefix in response, got: " + answer);
    }

    @Test
    @Property(name = AGENTIC_CONFIG_PREFIX + "creative-writer.chat-model", value = "creative-chat-model")
    @Property(name = AGENTIC_CONFIG_PREFIX + "creative-writer.temperature", value = "0.8")
    void demonstratesCreativeChatModelConfiguration(CreativeWriterAgent creativeWriterAgent) {
        var story = creativeWriterAgent.writeStory("dragons");
        assertTrue(story.startsWith("[creative]"), "Expected creative model prefix in response, got: " + story);
    }

    @Test
    @Property(name = AGENTIC_CONFIG_PREFIX + "greeter.chat-model", value = "professional-chat-model")
    void demonstratesProfessionalChatModelConfiguration(GreeterAgent greeterAgent) {
        var answer = greeterAgent.greet("Alex");
        assertTrue(answer.startsWith("[professional]"), "Expected professional model prefix in response, got: " + answer);
    }

    @Test
    @Property(name = AGENTIC_CONFIG_PREFIX + "supervisor.chat-model", value = "friendly-chat-model")
    @Property(name = AGENTIC_CONFIG_PREFIX + "supervisor.sub-agents", value = "greeter,creative-writer")
    void demonstratesSupervisorConfiguration(SupervisorAgent supervisorAgent) {
        var result = supervisorAgent.coordinate("prepare a plan");
        var extracted = extractFinalResultFromAgentInvocationJson(result);
        if (result.trim().isEmpty()) {
            throw new AssertionError("SupervisorAgent raw result is empty string. Possibly a broken agent wiring or factory.");
        }
        assertNotNull(extracted, "SupervisorAgent extracted value should not be null when raw result is: >>>" + result + "<<<");
        var clean = extracted.trim().replaceAll("^\"|\"$", "").replaceAll("\\r?\\n", "");
        assertTrue(
            clean.startsWith("I love it when a plan comes together."),
            "Expected supervisor to use friendly chat model, got extracted: '" + clean + "'; raw result: >>>" + result + "<<<"
        );
    }

    /**
     * Tries to extract result/final_result/finalResult from a JSON object string.
     * Returns null if not JSON or key not found.
     */
    private static String extractFinalResultFromAgentInvocationJson(String maybeJson) {
        if (maybeJson == null) return null;
        maybeJson = maybeJson.trim();
        if (!(maybeJson.startsWith("{") && maybeJson.endsWith("}"))) {
            return maybeJson;
        }
        try {
            // Parse with minimal logic (no dependencies)
            // Support for e.g. {"type":"agent_invocation_result","arguments":{"final_result":"[friendly] ..."}}
            var iArgs = maybeJson.indexOf("\"arguments\"");
            if (iArgs >= 0) {
                var brace = maybeJson.indexOf("{", iArgs);
                var endBrace = maybeJson.indexOf("}", brace);
                var argumentsPart = maybeJson.substring(brace + 1, endBrace);
                for (var key : new String[] { "final_result", "finalResult", "result" }) {
                    var k = argumentsPart.indexOf("\"" + key + "\"");
                    if (k >= 0) {
                        var vStart = argumentsPart.indexOf(":", k) + 1;
                        var vBegin = argumentsPart.indexOf("\"", vStart);
                        var vEnd = argumentsPart.indexOf("\"", vBegin + 1);
                        if (vBegin >= 0 && vEnd > vBegin) {
                            return argumentsPart.substring(vBegin + 1, vEnd);
                        } else {
                            // Try value until comma or end:
                            var comma = argumentsPart.indexOf(",", vStart);
                            if (comma < 0) comma = argumentsPart.length();
                            return argumentsPart.substring(vStart, comma).trim();
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    @Test
    void demonstratesDefaultConfigurationWithoutProperties() {
        // Start a minimal context with a single ChatModel candidate (unit)
        try (var ctx = ApplicationContext.builder()
                .deduceEnvironment(false)
                .environments("unit")
                .packages("io.micronaut.langchain4j.agentic")
                .start()) {
            var greeter = ctx.getBean(GreeterAgent.class);
            var answer = greeter.greet("Alex");
            assertTrue(answer.startsWith("[unit]"), "Expected default single-candidate ChatModel to be used, got: " + answer);
        }
    }


}
