package example.micronaut.decision

import dev.langchain4j.service.V
import dev.langchain4j.service.decision.Choice
import dev.langchain4j.service.decision.Decide
import io.micronaut.langchain4j.annotation.DecisionService

@DecisionService // <1>
interface TicketTriage {

    @Decide("Is this message spam?")
    fun isSpam(@V("message") message: String): Boolean // <2>

    @Decide("Which team should handle this ticket?")
    fun route(@V("ticket") ticket: String): Team // <3>

    @Decide("Which team should handle this ticket?")
    fun routeWithProbabilities(@V("ticket") ticket: String): Choice<Team> // <4>
}
