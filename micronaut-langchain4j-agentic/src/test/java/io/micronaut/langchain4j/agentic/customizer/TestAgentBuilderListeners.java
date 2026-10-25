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
package io.micronaut.langchain4j.agentic.customizer;

import dev.langchain4j.agentic.agent.AgentBuilder;
import dev.langchain4j.agentic.planner.AgenticService;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Test-only BeanCreatedEventListeners that customize Agentic builders automatically.
 * This replaces the previous AgenticServiceCustomizer SPI usage in tests.
 */
public final class TestAgentBuilderListeners {

    @Singleton
    @Requires(env = "customizer")
    static final class AgentBuilderListener implements BeanCreatedEventListener<AgentBuilder<?, ?>> {
        private final TestCustomizerTracker tracker;

        @Inject
        AgentBuilderListener(TestCustomizerTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public AgentBuilder<?, ?> onCreated(@NonNull BeanCreatedEvent<AgentBuilder<?, ?>> event) {
            tracker.increment();
            return event.getBean();
        }
    }

    @Singleton
    @Requires(env = "customizer")
    static final class SequentialBuildListener
        implements BeanCreatedEventListener<AgenticService<?, ?>> {
        private final TestCustomizerTracker tracker;

        @Inject
        SequentialBuildListener(TestCustomizerTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public AgenticService<?, ?> onCreated(
            @NonNull BeanCreatedEvent<AgenticService<?, ?>> event) {
            tracker.increment();
            return event.getBean();
        }
    }

}
