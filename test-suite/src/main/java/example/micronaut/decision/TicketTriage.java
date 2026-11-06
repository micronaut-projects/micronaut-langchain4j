package example.micronaut.decision;

import dev.langchain4j.service.V;
import dev.langchain4j.service.decision.Choice;
import dev.langchain4j.service.decision.Decide;
import io.micronaut.langchain4j.annotation.DecisionService;

@DecisionService // <1>
public interface TicketTriage {

    @Decide("Is this message spam?")
    boolean isSpam(@V("message") String message); // <2>

    @Decide("Which team should handle this ticket?")
    Team route(@V("ticket") String ticket); // <3>

    @Decide("Which team should handle this ticket?")
    Choice<Team> routeWithProbabilities(@V("ticket") String ticket); // <4>
}
