package example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest
@Disabled("Ollama Testcontainers broken?")
public class AiServiceTest {
    @Test
    void testAiService(Friend friend, ChatModel languageModel) {
        String result = friend.chat("Hello");

        assertNotNull(result);
        assertNotNull(languageModel);
    }
}
