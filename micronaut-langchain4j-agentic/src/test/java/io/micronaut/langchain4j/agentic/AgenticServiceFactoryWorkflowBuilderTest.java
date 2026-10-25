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

import dev.langchain4j.agentic.agent.AgentBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.langchain4j.agentic.shared.agent.GreeterAgent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class AgenticServiceFactoryWorkflowBuilderTest {

    @Test
    void workflowBuildersAreCreatedThroughBeanContext() {
        try (var ctx = ApplicationContext.run()) {
            assertNotNull(ctx.createBean(AgentBuilder.class, GreeterAgent.class));

            var builder = new AgenticServiceFactory.MicronautWorkflowAgentsBuilder(ctx);

            assertNotNull(builder.sequenceBuilder());
            assertNotNull(builder.sequenceBuilder(GreeterAgent.class));
            assertNotNull(builder.parallelBuilder());
            assertNotNull(builder.parallelBuilder(GreeterAgent.class));
            assertNotNull(builder.parallelMapperBuilder());
            assertNotNull(builder.parallelMapperBuilder(GreeterAgent.class));
            assertNotNull(builder.loopBuilder());
            assertNotNull(builder.loopBuilder(GreeterAgent.class));
            assertNotNull(builder.conditionalBuilder());
            assertNotNull(builder.conditionalBuilder(GreeterAgent.class));
        }
    }
}
