package example.micronaut.aiservice.rag

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.service.Result
import dev.langchain4j.store.embedding.EmbeddingStore
import io.micronaut.context.annotation.Property
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@Property(name = "spec.name", value = "RagTest")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RagTest implements OllamaTestPropertyProvider {

    @Test
    void retrievedContentIsPassedToTheModel(Expert expert, EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        TextSegment fact = TextSegment.from("Micronaut LangChain4j integrates LangChain4j with the Micronaut framework.")
        embeddingStore.add(embeddingModel.embed(fact).content(), fact) // <1>

        Result<String> result = expert.ask("What does Micronaut LangChain4j do?") // <2>

        assertNotNull(result.content())
        assertTrue(result.sources().any { it.textSegment().text() == fact.text() }) // <3>
    }
}
