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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false, environments = ["supervisor-test"])
internal class TravelSupervisorAgentTest {

    @Test
    fun supervisorInvokesTheSubAgents(supervisor: TravelSupervisorAgent) {
        assertEquals("Visit Italy and cook a pizza", supervisor.organize("I love jazz, plan my holidays"))
    }

    /**
     * Plays the planner of the supervisor: recommends a country, then a recipe, then answers.
     */
    @Singleton
    @Primary
    @Requires(env = ["supervisor-test"])
    class ScriptedPlannerChatModel : ChatModel {

        override fun doChat(request: ChatRequest): ChatResponse {
            val user = (request.messages().last() as UserMessage).singleText()
            val planner = request.messages().any { it is SystemMessage && it.text().contains("planner") }
            if (!planner) {
                return answer(if (user.contains("travel expert")) "Italy" else "Italy - Pizza")
            }
            return when {
                user.contains("'Italy - Pizza'") -> answer("""{"agentName": "done", "arguments": {"response": "Visit Italy and cook a pizza"}}""")
                user.contains("'Italy'") -> answer("""{"agentName": "suggestRecipe", "arguments": {"country": "Italy"}}""")
                else -> answer("""{"agentName": "recommendCountry", "arguments": {"topic": "jazz"}}""")
            }
        }

        private fun answer(text: String): ChatResponse = ChatResponse.builder().aiMessage(AiMessage.from(text)).build()
    }
}
