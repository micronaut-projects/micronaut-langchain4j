package example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

@MicronautTest
public class AiServiceTest {
    @Test
    void testAiService(Friend friend, ChatLanguageModel languageModel) {
        String result = friend.chat("Hello");

        assertNotNull(result);
        assertNotNull(languageModel);
    }
}
