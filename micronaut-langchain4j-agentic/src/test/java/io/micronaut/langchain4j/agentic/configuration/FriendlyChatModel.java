/*
 * Copyright 2017-2024 original authors
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
@Named("friendly-chat-model")
@Requires(env = "alternate")
public class FriendlyChatModel extends ConfigurableChatModel {

    public FriendlyChatModel() {
        super("friendly");
    }

    @Override
    public String chat(String userMessage) {
        if (isWorkflowCoordination(userMessage)) {
            return "I love it when a plan comes together.";
        }
        return super.chat(userMessage);
    }

    private boolean isWorkflowCoordination(String message) {
        var m = message == null ? "" : message.toLowerCase();
        // Heuristic to detect Supervisor planning prompt in tests
        return m.contains("coordinate") || m.contains("plan") || m.contains("supervisor");
    }

    @Override
    protected String generateResponse(String userMessage) {
        if (userMessage.contains("greet")) {
            return "Hello there! How can I help you today?";
        } else if (userMessage.contains("write")) {
            return "I'd be happy to help you write something creative!";
        } else {
            return "That's interesting! Tell me more.";
        }
    }
}
