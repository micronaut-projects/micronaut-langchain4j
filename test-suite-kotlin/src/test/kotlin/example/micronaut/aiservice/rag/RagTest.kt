package example.micronaut.aiservice.rag

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import io.micronaut.context.annotation.Property
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

@Property(name = "spec.name", value = "RagTest")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RagTest : OllamaTestPropertyProvider {

    @Test
    fun retrievedContentIsPassedToTheModel(expert: Expert, embeddingStore: EmbeddingStore<TextSegment>, embeddingModel: EmbeddingModel) {
        val fact = TextSegment.from("Micronaut LangChain4j integrates LangChain4j with the Micronaut framework.")
        embeddingStore.add(embeddingModel.embed(fact).content(), fact) // <1>

        val result = expert.ask("What does Micronaut LangChain4j do?") // <2>

        assertNotNull(result.content())
        assertTrue(result.sources().any { it.textSegment().text() == fact.text() }) // <3>
    }
}
