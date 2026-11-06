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
import dev.langchain4j.model.scoring.ScoringModel;
import io.micronaut.context.ApplicationContext;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = ReRankingRagTest.SPEC_NAME)
@Property(name = "langchain4j.rag.ingestion.docs.sources", value = "classpath:rag-docs")
@Property(name = "langchain4j.rag.content-retrievers.docs.max-results", value = "2")
@Property(name = "langchain4j.rag.retrieval-augmentors.docs.scoring-model", value = "keyword")
@Property(name = "langchain4j.rag.retrieval-augmentors.docs.max-results", value = "1")
@MicronautTest(startApplication = false, transactional = false)
class ReRankingRagTest {

    static final String SPEC_NAME = "ReRankingRagTest";

    @Inject
    ReRankedAssistant assistant;

    @Inject
    LastReRankedRequest lastRequest;

    @Test
    void reRanksTheRetrievedContentWithTheScoringModel() {
        assistant.chat("How do I build a native executable?");
        String userMessage = ((UserMessage) lastRequest.get().messages().getLast()).singleText();
        assertTrue(userMessage.contains("native images"), userMessage);
        assertFalse(userMessage.contains("JVM framework"), userMessage);
    }

    @Test
    void reRanksTheExplicitContentRetrieversAboveTheMinimumScore() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.rag.ingestion.docs.sources", "classpath:rag-docs",
            "langchain4j.rag.content-retrievers.docs.max-results", "2",
            "langchain4j.rag.retrieval-augmentors.explicit.content-retrievers", "docs",
            "langchain4j.rag.retrieval-augmentors.explicit.re-rank", "true",
            "langchain4j.rag.retrieval-augmentors.explicit.min-score", "0.5"))) {
            context.getBean(ExplicitReRankedAssistant.class).chat("How do I build a native executable?");
            String userMessage = ((UserMessage) context.getBean(LastReRankedRequest.class).get().messages().getLast()).singleText();
            assertTrue(userMessage.contains("native images"), userMessage);
            assertFalse(userMessage.contains("JVM framework"), userMessage);
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class LastReRankedRequest extends AtomicReference<ChatRequest> {
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Bean
        @Primary
        ChatModel chatModel(LastReRankedRequest lastRequest) {
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest request) {
                    lastRequest.set(request);
                    return ChatResponse.builder().aiMessage(AiMessage.from("ok")).build();
                }
            };
        }

        /**
         * Every text has the same embedding: the content retriever returns both documents, in no useful order.
         */
        @Bean
        @Primary
        EmbeddingModel embeddingModel() {
            return new EmbeddingModel() {
                @Override
                public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
                    return Response.from(textSegments.stream().map(segment -> Embedding.from(new float[] {1f, 0f})).toList());
                }
            };
        }

        @Singleton
        @Named("keyword")
        ScoringModel scoringModel() {
            return (segments, query) -> Response.from(segments.stream().map(segment -> segment.text().contains("native") ? 0.9 : 0.1).toList());
        }
    }
}

@Requires(property = "spec.name", value = ReRankingRagTest.SPEC_NAME)
@AiService("docs")
interface ReRankedAssistant {
    String chat(String userMessage);
}

@Requires(property = "spec.name", value = ReRankingRagTest.SPEC_NAME)
@AiService("explicit")
interface ExplicitReRankedAssistant {
    String chat(String userMessage);
}
