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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.event.AiServiceErrorEvent;
import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.event.AiServiceRequestIssuedEvent;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.event.AiServiceStartedEvent;
import dev.langchain4j.observability.api.event.InputGuardrailExecutedEvent;
import dev.langchain4j.observability.api.event.OutputGuardrailExecutedEvent;
import dev.langchain4j.observability.api.event.ToolCompensatedEvent;
import dev.langchain4j.observability.api.event.ToolExecutedEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import io.micronaut.context.BeanContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.List;

/**
 * Bridges the LangChain4j AI service events to Micronaut application events.
 *
 * <p>A LangChain4j listener is only registered for the event types that have at least one Micronaut
 * {@link ApplicationEventListener} (including {@code @EventListener} methods) for the event type or one of its super
 * types, so that the AI services do not create events nobody observes.</p>
 */
final class AiServiceEventPublishers {

    private static final List<Class<? extends AiServiceEvent>> EVENT_TYPES = List.of(
        AiServiceStartedEvent.class,
        AiServiceRequestIssuedEvent.class,
        AiServiceResponseReceivedEvent.class,
        AiServiceCompletedEvent.class,
        AiServiceErrorEvent.class,
        InputGuardrailExecutedEvent.class,
        OutputGuardrailExecutedEvent.class,
        ToolExecutedEvent.class,
        ToolCompensatedEvent.class
    );

    private AiServiceEventPublishers() {
    }

    static List<AiServiceListener<?>> create(BeanContext beanContext) {
        return EVENT_TYPES.stream()
            .filter(eventType -> hasListeners(beanContext, eventType))
            .<AiServiceListener<?>>map(eventType -> publisher(beanContext, eventType))
            .toList();
    }

    private static boolean hasListeners(BeanContext beanContext, Class<?> eventType) {
        return !beanContext.getBeanDefinitions(ApplicationEventListener.class, Qualifiers.byTypeArguments(eventType)).isEmpty();
    }

    private static <E extends AiServiceEvent> AiServiceListener<E> publisher(BeanContext beanContext, Class<E> eventType) {
        ApplicationEventPublisher<E> publisher = beanContext.getEventPublisher(eventType);
        return new AiServiceListener<>() {
            @Override
            public Class<E> getEventClass() {
                return eventType;
            }

            @Override
            public void onEvent(E event) {
                publisher.publishEvent(event);
            }
        };
    }
}
