package io.micronaut.langchain4j.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Retrieval augmented generation from the documents of the file system, with named embedding stores and models.
 */
class RagConfigurationTest {

    static final String SPEC_NAME = "RagConfigurationTest";

    @TempDir
    Path directory;

    @Test
    void loadsTheDocumentsOfADirectoryWithOrWithoutItsSubDirectories() throws IOException {
        writeDocuments();
        IngestionConfiguration configuration = new IngestionConfiguration("docs");
        configuration.setSources(List.of(directory.toString()));
        configuration.setGlob("*.txt");
        configuration.setRecursive(false);
        assertEquals(List.of("Micronaut is a JVM framework."), texts(DocumentIngestion.load(configuration)));

        configuration.setGlob("**.txt");
        configuration.setRecursive(true);
        assertEquals(2, DocumentIngestion.load(configuration).size());
    }

    @Test
    void ingestsAndRetrievesWithTheNamedStoreAndModel() throws IOException {
        writeDocuments();
        try (ApplicationContext context = ApplicationContext.run(Map.ofEntries(
            Map.entry("spec.name", SPEC_NAME),
            Map.entry("langchain4j.rag.ingestion.docs.sources", directory.toString()),
            Map.entry("langchain4j.rag.ingestion.docs.embedding-store", "documents"),
            Map.entry("langchain4j.rag.ingestion.docs.embedding-model", "constant"),
            Map.entry("langchain4j.rag.ingestion.docs.max-segment-size", "200"),
            Map.entry("langchain4j.rag.ingestion.docs.max-overlap-size", "20"),
            Map.entry("langchain4j.rag.ingestion.later.sources", directory.toString()),
            Map.entry("langchain4j.rag.ingestion.later.embedding-store", "later"),
            Map.entry("langchain4j.rag.ingestion.later.ingest-on-startup", "false"),
            Map.entry("langchain4j.rag.content-retrievers.docs.embedding-store", "documents"),
            Map.entry("langchain4j.rag.content-retrievers.docs.embedding-model", "constant")))) {
            IngestionConfiguration docs = context.getBean(IngestionConfiguration.class, Qualifiers.byName("docs"));
            assertEquals(200, docs.getMaxSegmentSize());
            assertEquals(20, docs.getMaxOverlapSize());
            assertTrue(docs.isIngestOnStartup());

            ContentRetriever retriever = context.getBean(ContentRetriever.class, Qualifiers.byName("docs"));
            assertEquals(2, retriever.retrieve(Query.from("framework")).size());

            assertFalse(context.getBean(IngestionConfiguration.class, Qualifiers.byName("later")).isIngestOnStartup());
            InMemoryEmbeddingStore<TextSegment> later = context.getBean(Argument.of(InMemoryEmbeddingStore.class), Qualifiers.byName("later"));
            assertTrue(later.search(dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[] {1f, 0f}))
                .build()).matches().isEmpty());
        }
    }

    private void writeDocuments() throws IOException {
        Files.writeString(directory.resolve("micronaut.txt"), "Micronaut is a JVM framework.");
        Path guides = Files.createDirectories(directory.resolve("guides"));
        Files.writeString(guides.resolve("native.txt"), "Micronaut applications can be compiled to native images.");
    }

    private static List<String> texts(List<Document> documents) {
        return documents.stream().map(Document::text).toList();
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Beans {

        @Singleton
        @Named("documents")
        EmbeddingStore<TextSegment> documents() {
            return new InMemoryEmbeddingStore<>();
        }

        @Singleton
        @Named("later")
        InMemoryEmbeddingStore<TextSegment> later() {
            return new InMemoryEmbeddingStore<>();
        }

        @Singleton
        @Named("constant")
        EmbeddingModel constant() {
            return new EmbeddingModel() {
                @Override
                public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
                    return Response.from(segments.stream().map(segment -> Embedding.from(new float[] {1f, 0f})).toList());
                }
            };
        }
    }
}
