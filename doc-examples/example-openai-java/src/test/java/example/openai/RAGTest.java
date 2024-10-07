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
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@MicronautTest
@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_OPEN_AI_API_KEY",
    matches = "\\.+"
)
public class RAGTest {
    boolean retrieved = false;

    @BeforeAll
    static void init(
        EmbeddingStore<TextSegment> embeddingStore,
        EmbeddingModel embeddingModel) throws IOException {
        assertInstanceOf(QdrantEmbeddingStore.class, embeddingStore);
        assertInstanceOf(E5SmallV2QuantizedEmbeddingModel.class, embeddingModel);
        System.out.println("Ingesting model");
        URL url = URI.create("https://github.com/glaforge/gemini-workshop-for-java-developers/raw/main/attention-is-all-you-need.pdf").toURL();
        ApachePdfBoxDocumentParser pdfParser = new ApachePdfBoxDocumentParser();
        Document document = pdfParser.parse(url.openStream());

        EmbeddingStoreIngestor storeIngestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(500, 100))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();
        storeIngestor.ingest(document);
        System.out.println("Model ingested");
    }

    @Test
    void testRAG(LlmExpert expert, EmbeddingModel embeddingModel) {
        assertInstanceOf(E5SmallV2QuantizedEmbeddingModel.class, embeddingModel);
        System.out.println("Asking LlmExpert");
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
