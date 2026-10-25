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

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Agent used to validate chat memory integration: calling say() twice should allow
 * the second call to access the previous turn via MessageWindowChatMemory.
 */
@AgenticService
public interface MemoryfulAgent {

    @UserMessage("{{content}}")
    @Agent
    String say(@V("content") String content);
}
