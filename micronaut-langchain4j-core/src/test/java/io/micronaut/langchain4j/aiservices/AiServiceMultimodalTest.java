package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.V;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.annotation.ImageUrl;
import io.micronaut.langchain4j.annotation.PdfUrl;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = AiServiceMultimodalTest.SPEC_NAME)
@MicronautTest(startApplication = false)
class AiServiceMultimodalTest {

    static final String SPEC_NAME = "AiServiceMultimodalTest";

    @Inject
    VisionAssistant assistant;

    @Test
    void sendsTheImageWithTheUserMessage() {
        assertEquals("text:Describe the picture|image:https://example.com/cat.png",
            assistant.describe("Describe the picture", "https://example.com/cat.png"));
    }

    @Test
    void sendsSeveralContents() {
        assertEquals("text:Compare the pictures|image:https://example.com/a.png|image:https://example.com/b.png",
            assistant.compare("Compare the pictures", List.of(URI.create("https://example.com/a.png"), URI.create("https://example.com/b.png"))));
    }

    @Test
    void sendsAPdfFile() {
        assertEquals("text:Summarize the report|pdf:https://example.com/report.pdf",
            assistant.summarize("report", URI.create("https://example.com/report.pdf")));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface VisionAssistant {
        String describe(@dev.langchain4j.service.UserMessage String question, @dev.langchain4j.service.UserMessage @ImageUrl String image);

        String compare(@dev.langchain4j.service.UserMessage String question, @dev.langchain4j.service.UserMessage @ImageUrl List<URI> images);

        @dev.langchain4j.service.UserMessage("Summarize the {{name}}")
        String summarize(@V("name") String name, @dev.langchain4j.service.UserMessage @PdfUrl URI pdf);
    }

    /**
     * Answers with the contents of the user message.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class EchoChatModel implements ChatModel {
        @Override
        public ChatResponse doChat(ChatRequest request) {
            UserMessage message = (UserMessage) request.messages().getLast();
            String contents = message.contents().stream().map(EchoChatModel::describe).collect(Collectors.joining("|"));
            return ChatResponse.builder().aiMessage(AiMessage.from(contents)).build();
        }

        private static String describe(Content content) {
            if (content instanceof TextContent text) {
                return "text:" + text.text();
            }
            if (content instanceof ImageContent image) {
                return "image:" + image.image().url();
            }
            if (content instanceof PdfFileContent pdf) {
                return "pdf:" + pdf.pdfFile().url();
            }
            return content.type().name();
        }
    }
}
