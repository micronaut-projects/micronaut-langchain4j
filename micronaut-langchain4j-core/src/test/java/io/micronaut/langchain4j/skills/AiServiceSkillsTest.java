package io.micronaut.langchain4j.skills;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = AiServiceSkillsTest.SPEC_NAME)
@Property(name = "langchain4j.skills.travel.classpath", value = "skills")
@MicronautTest(startApplication = false)
class AiServiceSkillsTest {

    static final String SPEC_NAME = "AiServiceSkillsTest";

    @Inject
    TravelAgent travelAgent;

    @Test
    void activatesTheSkillListedInTheSystemMessage() {
        assertEquals("List the clothes for the weather of the destination, then the travel documents.",
            travelAgent.chat("I am going to Iceland, what should I pack?"));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService(skills = "travel")
    interface TravelAgent {
        @dev.langchain4j.service.SystemMessage("You are a travel agent")
        String chat(String message);
    }

    /**
     * Activates the first skill listed in the system message, then answers with its instructions.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class SkillActivatingChatModel implements ChatModel {
        @Override
        public ChatResponse doChat(ChatRequest request) {
            if (request.messages().getLast() instanceof ToolExecutionResultMessage result) {
                return ChatResponse.builder().aiMessage(AiMessage.from(result.text().strip())).build();
            }
            String system = ((SystemMessage) request.messages().getFirst()).text();
            if (!system.startsWith("You are a travel agent") || !system.contains("packing")) {
                throw new IllegalStateException("The skills are not listed in the system message: " + system);
            }
            return ChatResponse.builder().aiMessage(AiMessage.from(ToolExecutionRequest.builder()
                .id("1")
                .name("activate_skill")
                .arguments("{\"skill_name\": \"packing\"}")
                .build())).build();
        }
    }
}
