package example.openai;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.e5smallv2q.E5SmallV2QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.testresources.qdrant.Qdrant;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.qdrant.client.grpc.Collections;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_OPEN_AI_API_KEY",
    matches = ".+"
)
class RAGTest implements TestPropertyProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RAGTest.class);
    boolean retrieved = false;

    @Override
    public @NonNull Map<String, String> getProperties() {
        return Qdrant.getProperties("mycollection", "384", Collections.Distance.Cosine.name());
    }

    static void init(
        EmbeddingStore<TextSegment> embeddingStore,
        EmbeddingModel embeddingModel) throws IOException {
        assertInstanceOf(QdrantEmbeddingStore.class, embeddingStore);
        assertInstanceOf(E5SmallV2QuantizedEmbeddingModel.class, embeddingModel);
        LOG.info("Ingesting model");
        URL url = URI.create("https://github.com/glaforge/gemini-workshop-for-java-developers/raw/main/attention-is-all-you-need.pdf").toURL();
        ApachePdfBoxDocumentParser pdfParser = new ApachePdfBoxDocumentParser();
        Document document = pdfParser.parse(url.openStream());

        EmbeddingStoreIngestor storeIngestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(500, 100))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();
        storeIngestor.ingest(document);
        LOG.info("Model ingested");
    }

    @Test
    void testRAG(LlmExpert expert,
                 EmbeddingStore<TextSegment> embeddingStore,
                 EmbeddingModel embeddingModel) throws IOException {
        init(embeddingStore, embeddingModel);
        assertInstanceOf(E5SmallV2QuantizedEmbeddingModel.class, embeddingModel);
        LOG.info("Asking LlmExpert");
        String result = expert.ask("What neural network architecture can be used for language models?");
        assertNotNull(result);
        assertTrue(retrieved);
    }

    @AiService
    interface LlmExpert {
        String ask(String question);
    }

    @Bean
    AiServiceCustomizer<LlmExpert> customizer(EmbeddingStore<TextSegment> embeddingStore,
                                              EmbeddingModel embeddingModel) {
        assertInstanceOf(E5SmallV2QuantizedEmbeddingModel.class, embeddingModel);
        assertInstanceOf(QdrantEmbeddingStore.class, embeddingStore);
        return (context ->
            context.aiServices()
                .contentRetriever(new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel) {
                    @Override
                    public List<Content> retrieve(Query query) {
                        retrieved = true;
                        return super.retrieve(query);
                    }
                })
        );
    }

    @Primary
    @Singleton
    EmbeddingModel inprocessModel() {
        return new E5SmallV2QuantizedEmbeddingModel();
    }
}
