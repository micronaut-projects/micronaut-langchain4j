package example.micronaut.aiservice;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Embedding model of the {@code scripted-test} environment: every text has the same embedding.
 */
@Singleton
@Primary
@Requires(env = "scripted-test")
public class ScriptedEmbeddingModel implements EmbeddingModel {

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        return Response.from(textSegments.stream().map(segment -> Embedding.from(new float[] {1f, 0f})).toList());
    }
}
