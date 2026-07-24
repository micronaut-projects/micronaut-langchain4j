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
package io.micronaut.langchain4j.agentic.creation;

import io.micronaut.langchain4j.agentic.shared.agent.CreativeWriterAgent;
import io.micronaut.langchain4j.agentic.shared.agent.GreeterAgent;
import io.micronaut.langchain4j.agentic.shared.agent.ExpertAgent;
import io.micronaut.langchain4j.agentic.shared.agent.StyledWriterAgent;
import io.micronaut.langchain4j.agentic.shared.agent.SupervisorAgent;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AgenticServiceCreationTest {

    @Test
    void greeterAgentBeanIsCreated() {
        try (var ctx = ApplicationContext.run()) {
            var agent = ctx.getBean(GreeterAgent.class);
            assertNotNull(agent);
        }
    }

    @Test
    void creativeWriterAgentBeanIsCreated() {
        try (var ctx = ApplicationContext.run()) {
            var agent = ctx.getBean(CreativeWriterAgent.class);
            assertNotNull(agent);
        }
    }

    @Test
    void expertAgentBeanIsCreated() {
        try (var ctx = ApplicationContext.run()) {
            var agent = ctx.getBean(ExpertAgent.class);
            assertNotNull(agent);
        }
    }

    @Test
    void supervisorAgentBeanIsCreated() {
        try (var ctx = ApplicationContext.run()) {
            var agent = ctx.getBean(SupervisorAgent.class);
            assertNotNull(agent);
        }
    }

    @Test
    void styledWriterAgentBeanIsCreated() {
        try (var ctx = ApplicationContext.run()) {
            var agent = ctx.getBean(StyledWriterAgent.class);
            assertNotNull(agent);
        }
    }
}
