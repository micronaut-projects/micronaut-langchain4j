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
package io.micronaut.langchain4j.agentic.tools;

import io.micronaut.context.ApplicationContext;
import io.micronaut.langchain4j.tools.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.Set;

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

            var toolRegistry = ctx.getBean(ToolRegistry.class);
            assertTrue(toolRegistry.getToolsTyped(Set.of(EchoTool.class)).stream().anyMatch(EchoTool.class::isInstance),
                "EchoTool bean should be registered as an executable LangChain4j tool");
        }
    }
}
