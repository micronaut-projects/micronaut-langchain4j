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
package io.micronaut.langchain4j.agentic.tools;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that tools declared on @AgenticService are wired via the AgenticServiceFactory
 * through ToolRegistry and that the interceptor does not require a ToolRegistry.
 */
class AgenticServiceToolsTest {

    @Test
    void toolsAreRequestedFromRegistryWhenBuildingAgent() {
        try (var ctx = ApplicationContext.run("tools")) {
            // Bean lookup triggers agent build via interceptor + factory
            var agent = ctx.getBean(ToolsEchoAgent.class);
            // Trigger agent building via the interceptor, which will configure tools via ToolRegistry
            agent.echo("ping");

            // Ensure the ToolRegistry (replaced in tests) was invoked with the requested tool type
            var counting = ctx.getBean(CountingToolRegistry.class);
            assertTrue(counting.getInvocations() >= 1, "ToolRegistry.getToolsTyped should be invoked at least once");
            var requested = counting.getLastRequestedTypes();
            assertNotNull(requested, "Requested tool types should be recorded");
            assertTrue(requested.contains(EchoTool.class), "EchoTool should be requested as part of tools list");
        }
    }
}
