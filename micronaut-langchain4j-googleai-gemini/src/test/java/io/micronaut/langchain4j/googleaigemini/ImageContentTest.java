package io.micronaut.langchain4j.googleaigemini;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_GOOGLE_AI_GEMINI_API_KEY",
    matches = ".+"
)
@Property(name = "langchain4j.google-ai-gemini.model-name", value = "gemini-2.5-flash")
@Property(name = "langchain4j.google-ai-gemini.chat-model.log-requests", value = StringUtils.TRUE)
@Property(name = "langchain4j.google-ai-gemini.chat-model.log-responses", value = StringUtils.TRUE)
@Property(name = "spec.name", value = "ImageContentTest")
@MicronautTest(startApplication = false)
@Disabled
class ImageContentTest {
    @Test
    void testImage(ChatService chatService) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.jpg")) {
            if (is == null) {
                throw new IllegalStateException("Resource cat.jpg not found");
            }

            var imageContent = imageContent(is, "image/jpeg");
            String response = assertDoesNotThrow(() -> chatService.chat("What animal is shown in this image?", List.of(imageContent)));
            assertTrue(response.toLowerCase(Locale.ROOT).contains("cat"), response + " did not contain 'cat'");
        }
    }

    @Requires(property = "spec.name", value = "ImageContentTest")
    @AiService
    interface ChatService {
        @SystemMessage("You are a helpful AI assistant.")
        String chat(@UserMessage String userMessage, @UserMessage List<ImageContent> images);
    }


    private static ImageContent imageContent(InputStream is, String mediaType) throws IOException {
        byte[] bytes = is.readAllBytes();
        return imageContent(bytes, mediaType);
    }

    private static ImageContent imageContent(byte[] imageBytes, String mediaType) {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return imageContent(base64, mediaType);
    }

    private static ImageContent imageContent(String base64, String mediaType) {
        return ImageContent.from(Image.builder()
            .base64Data(base64)
            .mimeType(mediaType)
            .build());
    }
}
