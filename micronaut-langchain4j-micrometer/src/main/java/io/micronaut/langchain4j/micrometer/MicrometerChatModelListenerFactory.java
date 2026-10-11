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
package io.micronaut.langchain4j.micrometer;

import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.observation.listener.ObservationChatModelListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

/**
 * Registers the LangChain4j {@link ChatModelListener} that record the
 * <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/">OpenTelemetry GenAI metrics</a>
 * of the chat models. The listeners are injected into every chat model.
 *
 * <p>When an {@link ObservationRegistry} bean and {@code langchain4j-observation} are present, the
 * {@link ObservationChatModelListener} is registered instead: it records the chat model requests as observations,
 * which produce both the metrics and, with a tracing bridge, spans.</p>
 */
@Factory
@Internal
@Requires(beans = MeterRegistry.class)
@Requires(property = MicrometerChatModelListenerFactory.ENABLED, notEquals = StringUtils.FALSE)
final class MicrometerChatModelListenerFactory {

    static final String ENABLED = "langchain4j.micrometer.enabled";

    @Singleton
    @Requires(missingBeans = ObservationRegistry.class)
    MicrometerMetricsChatModelListener metricsChatModelListener(MeterRegistry meterRegistry) {
        return new MicrometerMetricsChatModelListener(meterRegistry);
    }

    @Singleton
    @Requires(classes = {ObservationChatModelListener.class, ObservationRegistry.class})
    @Requires(beans = ObservationRegistry.class)
    ObservationChatModelListener observationChatModelListener(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        return new ObservationChatModelListener(observationRegistry, meterRegistry);
    }
}
