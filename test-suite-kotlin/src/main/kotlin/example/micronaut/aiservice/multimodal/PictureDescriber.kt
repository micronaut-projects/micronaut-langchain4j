package example.micronaut.aiservice.multimodal

import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import io.micronaut.langchain4j.annotation.AiService
import io.micronaut.langchain4j.annotation.ImageUrl
import io.micronaut.langchain4j.annotation.PdfUrl
import java.net.URI

@AiService
interface PictureDescriber {

    @SystemMessage("You describe pictures for visually impaired people")
    fun describe(@UserMessage question: String,
                 @UserMessage @ImageUrl picture: String): String // <1>

    @UserMessage("Summarize the {{topic}} of this document")
    fun summarize(@V("topic") topic: String,
                  @UserMessage @PdfUrl document: URI): String // <2>
}
