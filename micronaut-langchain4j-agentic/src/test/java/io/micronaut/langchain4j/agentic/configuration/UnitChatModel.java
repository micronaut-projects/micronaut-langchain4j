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

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import jakarta.inject.Named;
/**
 * Test ChatModel used in the "unit" environment to verify default
 * configuration behavior when only a single ChatModel candidate exists.
 */
@Singleton
@Primary
@Named("unit-chat-model")
@Requires(env = "unit")
public final class UnitChatModel extends ConfigurableChatModel {

    UnitChatModel() {
        super("unit");
    }

    @Override
    protected String generateResponse(String userMessage) {
        if (userMessage.contains("greet")) {
            return "Hello from unit!";
        } else if (userMessage.contains("write")) {
            return "Unit writer at your service!";
        } else {
            return "Unit default.";
        }
    }
}
