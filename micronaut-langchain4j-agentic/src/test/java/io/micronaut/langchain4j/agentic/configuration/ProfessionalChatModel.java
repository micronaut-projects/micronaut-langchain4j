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
package io.micronaut.langchain4j.agentic.configuration;

import jakarta.inject.Singleton;
import jakarta.inject.Named;
import io.micronaut.context.annotation.Requires;

@Singleton
@Named("professional-chat-model")
@Requires(env = "alternate")
public class ProfessionalChatModel extends ConfigurableChatModel {

    public ProfessionalChatModel() {
        super("professional");
    }

    @Override
    protected String generateResponse(String userMessage) {
        if (userMessage.contains("greet")) {
            return "Greetings. How may I assist you?";
        } else if (userMessage.contains("write")) {
            return "I will assist with your writing requirements.";
        } else {
            return "Understood. Please provide additional details.";
        }
    }
}
