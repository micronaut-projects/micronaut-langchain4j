package example.micronaut.aiservice.multimodal

import dev.langchain4j.data.message.AiMessage
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

@Property(name = "spec.name", value = "PictureDescriberTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false)
internal class PictureDescriberTest {

    @Test
    fun sendsThePicture(describer: PictureDescriber) {
        assertEquals("text:What is it?|image:https://example.com/cat.png",
            describer.describe("What is it?", "https://example.com/cat.png"))
    }

    @Test
    fun sendsTheDocument(describer: PictureDescriber) {
        assertEquals("text:Summarize the risks of this document|pdf:https://example.com/report.pdf",
            describer.summarize("risks", URI.create("https://example.com/report.pdf")))
    }

    /**
     * Answers with the contents of the user message.
     */
    @Singleton
    @Primary
    @Requires(property = "spec.name", value = "PictureDescriberTest")
    class EchoChatModel : ChatModel {
        override fun doChat(request: ChatRequest): ChatResponse {
            val message = request.messages().last() as UserMessage
            val described = message.contents().joinToString("|") {
                when (it) {
                    is TextContent -> "text:${it.text()}"
                    is ImageContent -> "image:${it.image().url()}"
                    is PdfFileContent -> "pdf:${it.pdfFile().url()}"
                    else -> it.type().name
                }
            }
            return ChatResponse.builder().aiMessage(AiMessage.from(described)).build()
        }
    }
}
