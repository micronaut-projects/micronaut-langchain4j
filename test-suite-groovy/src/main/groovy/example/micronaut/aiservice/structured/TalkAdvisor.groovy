package example.micronaut.aiservice.structured

import example.structured.TalkRecommendation
import dev.langchain4j.service.SystemMessage
import io.micronaut.langchain4j.annotation.AiService

@AiService
interface TalkAdvisor {

    @SystemMessage("You recommend conference talks to attendees.")
    TalkRecommendation recommend(String interests) // <1>

    @SystemMessage("You recommend conference talks to attendees.")
    List<TalkRecommendation> shortlist(String interests) // <2>
}
