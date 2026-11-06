package example.micronaut.tutorial

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.output.Response
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.test.ScriptedChatModel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "KnowledgeAssistantTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@Property(name = "langchain4j.rag.ingestion.knowledge.sources", value = "classpath:tutorial-docs") // <1>
@Property(name = "langchain4j.rag.content-retrievers.knowledge.max-results", value = "1") // <2>
@MicronautTest(startApplication = false)
internal class KnowledgeAssistantTest {

    @Inject
    lateinit var assistant: KnowledgeAssistant

    @Test
    fun answersFromTheDocumentsAndTheTools() {
        val result = assistant.ask("What is the latest release?") // <3>

        assertTrue(result.sources().any { it.textSegment().text().contains("GraalVM") }) // <4>
        assertEquals("tool:2.4.0", result.content()) // <5>
    }

    @Factory
    @Requires(property = "spec.name", value = "KnowledgeAssistantTest")
    class Models {

        @Bean
        @Primary
        fun chatModel(): ChatModel =
            ScriptedChatModel.builder()
                .whenToolResult().respond { request -> "tool:" + ScriptedChatModel.lastToolResult(request) }
                .whenUserMessageContains("latest release").callTool("latestRelease", "{}")
                .build()

        @Bean
        @Primary
        fun embeddingModel(): EmbeddingModel = object : EmbeddingModel {
            override fun embedAll(segments: List<TextSegment>): Response<List<Embedding>> =
                Response.from(segments.map { Embedding.from(floatArrayOf(1f, 0f)) })
        }
    }
}
