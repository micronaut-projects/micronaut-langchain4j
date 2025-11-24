package io.micronaut.langchain4j.googleaigemini;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.utils.ImageContentUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_GOOGLE_AI_GEMINI_API_KEY",
    matches = ".+"
)
@Property(name = "langchain4j.google-ai-gemini.chat-model.log-requests", value = StringUtils.TRUE)
@Property(name = "langchain4j.google-ai-gemini.chat-model.log-responses", value = StringUtils.TRUE)
@Property(name = "spec.name", value = "GoogleAiGeminiImageContentTest")
@MicronautTest(startApplication = false)
class GoogleAiGeminiImageContentTest {
    @Inject
    BeanContext beanContext;

    @Test
    void testImage(ChatService chatService) throws IOException {
        assertTrue(beanContext.containsBean(ChatModel.class));
        assertFalse(beanContext.containsBean(EmbeddingStore.class));
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("cat.jpg")) {
            if (is == null) {
                throw new IllegalStateException("Resource cat.jpg not found");
            }

            var imageContent = ImageContentUtils.imageContent(is, "image/jpeg");
            String response = assertDoesNotThrow(() -> chatService.chat("What animal is shown in this image?", List.of(imageContent)));
            assertTrue(response.toLowerCase(Locale.ROOT).contains("cat"), response + " did not contain 'cat'");
        }
    }

    @Requires(property = "spec.name", value = "GoogleAiGeminiImageContentTest")
    @AiService
    interface ChatService {
        @SystemMessage("You are a helpful AI assistant.")
        String chat(@UserMessage String userMessage, @UserMessage List<ImageContent> images);
    }
}
