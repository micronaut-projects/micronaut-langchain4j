package example.openai;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.e5smallv2q.E5SmallV2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
@Requires(property = "langchain4j.open-ai.api-key")
public class ToolTest {
    @Inject
    ApplicationContext applicationContext;

    @Test
    void testInjectTools() {
        MathGenius mathGenius = applicationContext.getBean(MathGenius.class);
        Assertions.assertNotNull(mathGenius);
        String result = mathGenius.ask("What is the square root of 475695037565?");
        Assertions.assertNotNull(result);
    }

    @Primary
    @Singleton
    EmbeddingModel inprocessModel() {
        return new E5SmallV2QuantizedEmbeddingModel();
    }

    @Bean
    AiServiceCustomizer<MathGenius> customizer(EmbeddingStore<TextSegment> embeddingStore,
                                                      EmbeddingModel embeddingModel) {
        assertInstanceOf(E5SmallV2QuantizedEmbeddingModel.class, embeddingModel);
        assertInstanceOf(QdrantEmbeddingStore.class, embeddingStore);
        return creationContext -> {
            // no-op, just used for assertions
        };
    }

}
