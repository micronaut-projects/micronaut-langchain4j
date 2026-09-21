package example.micronaut.aiservice.evaluation

import dev.langchain4j.model.chat.ChatModel
import io.micronaut.context.annotation.Property
import io.micronaut.langchain4j.evaluation.EvaluationRequest
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

@Property(name = "spec.name", value = "AiServiceEvaluationExample")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AiServiceEvaluationExample : OllamaTestPropertyProvider {

    @Test
    fun evaluatesAiServiceResponse(friend: EvaluatingFriend, chatModel: ChatModel) {
        val userText = "Reply with exactly: Micronaut is a JVM framework."
        val response = friend.chat(userText)

        val evaluator = RelevancyEvaluator(chatModel)
        val evaluation = evaluator.evaluate(EvaluationRequest.from(userText, response))

        assertNotNull(evaluation)
        assertFalse(evaluation.feedback().isBlank())
    }
}
