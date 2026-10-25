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

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Agent used in tests to validate that tools are wired via ToolRegistry through the factory.
 */
@AgenticService(tools = { EchoTool.class })
public interface ToolsEchoAgent {

    @UserMessage("Echo {{input}} using the echo tool")
    @Agent(description = "Echoes the given input using a tool")
    String echo(@V("input") String input);
}
