package io.micronaut.langchain4j.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = ConfiguredRagTest.SPEC_NAME)
@Property(name = "langchain4j.rag.ingestion.docs.sources", value = "classpath:rag-docs")
@Property(name = "langchain4j.rag.content-retrievers.docs.max-results", value = "1")
@Property(name = "langchain4j.rag.content-retrievers.docs.min-score", value = "0.5")
@MicronautTest(startApplication = false, transactional = false)
class ConfiguredRagTest {

    static final String SPEC_NAME = "ConfiguredRagTest";

    @Inject
    DocsAssistant assistant;

    @Inject
    LastRequest lastRequest;

    @Inject
    @Named("docs")
    ContentRetriever contentRetriever;

    @Inject
    @Named("docs")
    EmbeddingStoreIngestor ingestor;

    @Test
    void ingestsTheDocumentsOnStartupAndRetrievesTheRelevantSegment() {
        List<String> retrieved = contentRetriever.retrieve(Query.from("What is Micronaut?")).stream()
            .map(content -> content.textSegment().text())
            .toList();
        assertEquals(1, retrieved.size());
        assertTrue(retrieved.getFirst().contains("JVM framework"), retrieved::toString);
        assertNotNull(ingestor);
    }

    @Test
    void theAiServiceNamedAfterTheContentRetrieverUsesIt() {
        assistant.chat("What is Micronaut?");
        String userMessage = ((UserMessage) lastRequest.get().messages().getLast()).singleText();
        assertTrue(userMessage.contains("JVM framework"), userMessage);
        assertFalse(userMessage.contains("native images"), userMessage);
    }

    @Test
    void loadsTheDocumentsOfTheSources() {
        IngestionConfiguration configuration = new IngestionConfiguration("test");
        configuration.setSources(List.of("classpath:rag-docs"));
        configuration.setGlob("graal*");
        assertEquals(1, DocumentIngestion.load(configuration).size());
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class LastRequest extends AtomicReference<ChatRequest> {
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Bean
        @Primary
        ChatModel chatModel(LastRequest lastRequest) {
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest request) {
                    lastRequest.set(request);
                    return ChatResponse.builder().aiMessage(AiMessage.from("ok")).build();
                }
            };
        }

        /**
         * Embeds a text as the presence of two keywords, so that the similarity of the test documents is predictable.
         */
        @Bean
        @Primary
        EmbeddingModel embeddingModel() {
            return new EmbeddingModel() {
                @Override
                public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
                    return Response.from(textSegments.stream().map(segment -> embedding(segment.text())).toList());
                }

                private Embedding embedding(String text) {
                    String lowerCase = text.toLowerCase(Locale.ROOT);
                    return Embedding.from(new float[] {
                        lowerCase.contains("micronaut") ? 1f : 0f,
                        lowerCase.contains("graalvm") || lowerCase.contains("native") ? 1f : 0f,
                        0.01f
                    });
                }
            };
        }
    }
}

@Requires(property = "spec.name", value = ConfiguredRagTest.SPEC_NAME)
@AiService("docs")
interface DocsAssistant {
    String chat(String userMessage);
}
