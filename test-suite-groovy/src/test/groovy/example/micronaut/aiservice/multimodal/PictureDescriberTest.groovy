package example.micronaut.aiservice.multimodal

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.Content
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.data.message.PdfFileContent
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

@Property(name = "spec.name", value = "PictureDescriberTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false)
class PictureDescriberTest {

    @Test
    void sendsThePicture(PictureDescriber describer) {
        assertEquals("text:What is it?|image:https://example.com/cat.png",
            describer.describe("What is it?", "https://example.com/cat.png"))
    }

    @Test
    void sendsTheDocument(PictureDescriber describer) {
        assertEquals("text:Summarize the risks of this document|pdf:https://example.com/report.pdf",
            describer.summarize("risks", URI.create("https://example.com/report.pdf")))
    }

    /**
     * Answers with the contents of the user message.
     */
    @Singleton
    @Primary
    @Requires(property = "spec.name", value = "PictureDescriberTest")
    static class EchoChatModel implements ChatModel {
        @Override
        ChatResponse doChat(ChatRequest request) {
            UserMessage message = (UserMessage) request.messages().last()
            String described = message.contents().collect { Content content ->
                if (content instanceof TextContent) {
                    return "text:" + content.text()
                }
                if (content instanceof ImageContent) {
                    return "image:" + content.image().url()
                }
                if (content instanceof PdfFileContent) {
                    return "pdf:" + content.pdfFile().url()
                }
                content.type().name()
            }.join("|")
            ChatResponse.builder().aiMessage(AiMessage.from(described)).build()
        }
    }
}
