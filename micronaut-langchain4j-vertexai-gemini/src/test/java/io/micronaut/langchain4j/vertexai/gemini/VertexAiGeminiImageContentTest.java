package io.micronaut.langchain4j.vertexai.gemini;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.utils.ImageContentUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_VERTEX_AI_GEMINI_PROJECT",
    matches = ".+"
)
@Property(name = "langchain4j.vertex-ai-gemini.location", value = "us-central1")
@Property(name = "langchain4j.vertex-ai-gemini.model-name", value = "gemini-2.5-flash")
@Property(name = "langchain4j.vertex-ai-gemini.chat-model.log-requests", value = StringUtils.TRUE)
@Property(name = "langchain4j.vertex-ai-gemini.chat-model.log-responses", value = StringUtils.TRUE)
@Property(name = "spec.name", value = "VertexAiGeminiImageContentTest")
@MicronautTest(startApplication = false)
class VertexAiGeminiImageContentTest {
    @Test
    void testImage(ChatService chatService) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.jpg")) {
            if (is == null) {
                throw new IllegalStateException("Resource cat.jpg not found");
            }

            var imageContent = ImageContentUtils.imageContent(is, "image/jpeg");
            String response = assertDoesNotThrow(() -> chatService.chat("What animal is shown in this image?", List.of(imageContent)));
            assertTrue(response.toLowerCase(Locale.ROOT).contains("cat"), response + " did not contain 'cat'");
        }
    }

    @Requires(property = "spec.name", value = "VertexAiGeminiImageContentTest")
    @AiService
    interface ChatService {
        @SystemMessage("You are a helpful AI assistant.")
        String chat(@UserMessage String userMessage, @UserMessage List<ImageContent> images);
    }
}
