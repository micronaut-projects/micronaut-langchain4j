package example.micronaut.tutorial;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.Result;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.test.ScriptedChatModel;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "KnowledgeAssistantTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@Property(name = "langchain4j.rag.ingestion.knowledge.sources", value = "classpath:tutorial-docs") // <1>
@Property(name = "langchain4j.rag.content-retrievers.knowledge.max-results", value = "1") // <2>
@MicronautTest(startApplication = false)
class KnowledgeAssistantTest {

    @Inject
    KnowledgeAssistant assistant;

    @Test
    void answersFromTheDocumentsAndTheTools() {
        Result<String> result = assistant.ask("What is the latest release?"); // <3>

        assertTrue(result.sources().stream()
            .anyMatch(source -> source.textSegment().text().contains("GraalVM"))); // <4>
        assertEquals("tool:2.4.0", result.content()); // <5>
    }

    @Factory
    @Requires(property = "spec.name", value = "KnowledgeAssistantTest")
    static class Models {

        @Bean
        @Primary
        ChatModel chatModel() {
            return ScriptedChatModel.builder()
                .whenToolResult().respond(request -> "tool:" + ScriptedChatModel.lastToolResult(request))
                .whenUserMessageContains("latest release").callTool("latestRelease", "{}")
                .build();
        }

        @Bean
        @Primary
        EmbeddingModel embeddingModel() {
            return new EmbeddingModel() {
                @Override
                public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
                    return Response.from(segments.stream()
                        .map(segment -> Embedding.from(new float[] {1f, 0f}))
                        .toList());
                }
            };
        }
    }
}
