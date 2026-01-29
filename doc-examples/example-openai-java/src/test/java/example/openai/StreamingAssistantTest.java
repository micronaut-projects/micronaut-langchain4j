package example.openai;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_OPEN_AI_API_KEY",
    matches = "\\.+"
)
public class StreamingAssistantTest {

    @Inject
    StreamingAssistant streamingAssistant; // <1>

    @Test
    void testStreamingChat() {
        assertNotNull(streamingAssistant);

        Flux<String> response = streamingAssistant.chat("user123", "Tell me a very short joke"); // <2>

        StepVerifier.create(response)
            .expectNextMatches(chunk -> chunk != null && !chunk.isEmpty()) // <3>
            .thenConsumeWhile(chunk -> true) // <4>
            .verifyComplete(); // <5>
    }
}
