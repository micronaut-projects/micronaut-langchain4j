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
package io.micronaut.langchain4j.controlpanel;

import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.controlpanel.core.security.ControlPanelSecurityPaths;
import io.micronaut.controlpanel.core.security.ControlPanelWriteAccess;
import io.micronaut.controlpanel.core.security.ControlPanelWriteAccessEvaluator;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

/**
 * Sends the messages of the chat of the control panel to a chat model.
 */
@Controller(ControlPanelSecurityPaths.CONTROL_PANEL + LangChain4jControlPanelController.PATH)
@ExecuteOn(TaskExecutors.BLOCKING)
@Internal
@Requires(property = LangChain4jControlPanel.ENABLED_PROPERTY, value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
final class LangChain4jControlPanelController {

    static final String PATH = "/langchain4j-control-panel-controller";

    private final BeanContext beanContext;
    private final ControlPanelWriteAccessEvaluator writeAccessEvaluator;

    LangChain4jControlPanelController(BeanContext beanContext, ControlPanelWriteAccessEvaluator writeAccessEvaluator) {
        this.beanContext = beanContext;
        this.writeAccessEvaluator = writeAccessEvaluator;
    }

    /**
     * Sends a message to a chat model. Calling a model costs tokens, so it is only allowed with write access to the
     * control panel.
     *
     * @param request The HTTP request
     * @param message The message and the name of the chat model
     * @return The answer of the model
     */
    @Post("/chat")
    HttpResponse<ChatReply> chat(HttpRequest<?> request, @Body ChatMessage message) {
        ControlPanelWriteAccess writeAccess = writeAccessEvaluator.evaluate(request);
        if (!writeAccess.allowed()) {
            return HttpResponse.<ChatReply>status(HttpStatus.FORBIDDEN).body(new ChatReply(null, writeAccess.reason()));
        }
        Optional<ChatModel> model = beanContext.getBeanRegistrations(ChatModel.class).stream()
            .filter(registration -> LangChain4jControlPanel.name(registration).equals(message.model()))
            .map(BeanRegistration::getBean)
            .findFirst();
        if (model.isEmpty()) {
            return HttpResponse.notFound(new ChatReply(null, "No chat model named " + message.model()));
        }
        try {
            return HttpResponse.ok(new ChatReply(model.get().chat(message.text()), null));
        } catch (RuntimeException e) {
            return HttpResponse.<ChatReply>serverError().body(new ChatReply(null, e.getMessage()));
        }
    }

    /**
     * A message sent to a chat model.
     *
     * @param model The name of the chat model bean
     * @param text The message
     */
    @Serdeable
    record ChatMessage(String model, String text) {
    }

    /**
     * The answer of a chat model.
     *
     * @param answer The answer
     * @param error The error, if the model failed
     */
    @Serdeable
    record ChatReply(@Nullable String answer, @Nullable String error) {
    }
}
