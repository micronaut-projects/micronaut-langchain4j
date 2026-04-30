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

import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.guardrail.GuardrailService;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.BeanDefinition;

import java.util.concurrent.atomic.AtomicReference;

final class MicronautAiServiceContext extends AiServiceContext {
    private final GuardrailService.Builder micronautGuardrailServiceBuilder;
    private final AtomicReference<GuardrailService> guardrailService = new AtomicReference<>();

    MicronautAiServiceContext(Class<?> aiServiceClass, BeanDefinition<?> beanDefinition, BeanContext beanContext) {
        super(aiServiceClass);
        this.micronautGuardrailServiceBuilder = new MicronautGuardrailServiceBuilder(beanDefinition, beanContext);
    }

    @Override
    public GuardrailService guardrailService() {
        return guardrailService.updateAndGet(service ->
            service != null ? service : micronautGuardrailServiceBuilder.build());
    }
}
