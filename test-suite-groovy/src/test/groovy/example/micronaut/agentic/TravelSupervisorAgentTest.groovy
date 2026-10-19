package example.micronaut.agentic

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false, environments = "supervisor-test")
class TravelSupervisorAgentTest {

    @Test
    void supervisorInvokesTheSubAgents(TravelSupervisorAgent supervisor) {
        assertEquals("Visit Italy and cook a pizza", supervisor.organize("I love jazz, plan my holidays"))
    }

    /**
     * Plays the planner of the supervisor: recommends a country, then a recipe, then answers.
     */
    @Singleton
    @Primary
    @Requires(env = "supervisor-test")
    static class ScriptedPlannerChatModel implements ChatModel {

        @Override
        ChatResponse doChat(ChatRequest request) {
            String user = ((UserMessage) request.messages().last()).singleText()
            boolean planner = request.messages().any { it instanceof SystemMessage && it.text().contains("planner") }
            if (!planner) {
                return answer(user.contains("travel expert") ? "Italy" : "Italy - Pizza")
            }
            if (user.contains("'Italy - Pizza'")) {
                return answer('{"agentName": "done", "arguments": {"response": "Visit Italy and cook a pizza"}}')
            }
            if (user.contains("'Italy'")) {
                return answer('{"agentName": "suggestRecipe", "arguments": {"country": "Italy"}}')
            }
            answer('{"agentName": "recommendCountry", "arguments": {"topic": "jazz"}}')
        }

        private static ChatResponse answer(String text) {
            ChatResponse.builder().aiMessage(AiMessage.from(text)).build()
        }
    }
}
