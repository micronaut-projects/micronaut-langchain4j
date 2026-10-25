package example.micronaut.aiservice.structured

import example.structured.TalkRecommendation
import dev.langchain4j.service.SystemMessage
import io.micronaut.langchain4j.annotation.AiService

@AiService
interface TalkAdvisor {

    @SystemMessage("You recommend conference talks to attendees.")
    fun recommend(interests: String): TalkRecommendation // <1>

    @SystemMessage("You recommend conference talks to attendees.")
    fun shortlist(interests: String): List<TalkRecommendation> // <2>
}
