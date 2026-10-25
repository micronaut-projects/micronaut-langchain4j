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
package io.micronaut.langchain4j.agentic;

import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

final class AgenticServiceInterceptorTest {

    @Test
    void resolvesAgentInterfaceFromInterceptedImplementation() {
        assertSame(TestAgent.class, AgenticServiceInterceptor.resolveAgentInterface(TestAgent.class));
        assertSame(TestAgent.class, AgenticServiceInterceptor.resolveAgentInterface(TestAgentImpl.class));
        assertSame(PlainType.class, AgenticServiceInterceptor.resolveAgentInterface(PlainType.class));
    }

    @AgenticService
    interface TestAgent {
    }

    static final class TestAgentImpl implements TestAgent {
    }

    static final class PlainType {
    }
}
