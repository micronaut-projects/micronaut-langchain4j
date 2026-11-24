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
package io.micronaut.langchain4j.dev;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.ollama.OllamaChatModel;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Customizes the builder of the Ollama chat model, as a listener of the application may, with a chat model listener
 * the context does not see: the tests treat its class as a class of the application.
 */
@Singleton
@Requires(property = "test.builder-listener", value = "true")
public class OllamaBuilderListener implements BeanCreatedEventListener<OllamaChatModel.OllamaChatModelBuilder> {
    @Override
    public OllamaChatModel.OllamaChatModelBuilder onCreated(BeanCreatedEvent<OllamaChatModel.OllamaChatModelBuilder> event) {
        return event.getBean().listeners(List.of(new ChatModelListener() { }));
    }
}
