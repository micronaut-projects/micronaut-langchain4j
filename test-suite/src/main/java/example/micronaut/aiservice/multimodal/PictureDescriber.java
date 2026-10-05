package example.micronaut.aiservice.multimodal;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.annotation.ImageUrl;
import io.micronaut.langchain4j.annotation.PdfUrl;

import java.net.URI;

@AiService
public interface PictureDescriber {

    @SystemMessage("You describe pictures for visually impaired people")
    String describe(@UserMessage String question,
                    @UserMessage @ImageUrl String picture); // <1>

    @UserMessage("Summarize the {{topic}} of this document")
    String summarize(@V("topic") String topic,
                     @UserMessage @PdfUrl URI document); // <2>
}
