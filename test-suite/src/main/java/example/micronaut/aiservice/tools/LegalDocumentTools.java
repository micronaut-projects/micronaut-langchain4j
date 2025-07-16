package example.micronaut.aiservice.tools;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

import java.time.LocalDate;

@Singleton // <1>
public class LegalDocumentTools {
    @Tool("Returns the last time the PRIVACY document was updated") // <2>
    public LocalDate lastUpdatePrivacy() {
        return LocalDate.of(2013, 3, 9); // <3>
    }
}
