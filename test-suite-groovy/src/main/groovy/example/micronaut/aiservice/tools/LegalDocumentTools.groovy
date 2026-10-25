package example.micronaut.aiservice.tools

import dev.langchain4j.agent.tool.Tool
import groovy.transform.CompileStatic
import jakarta.inject.Singleton

import java.time.LocalDate

@Singleton // <1>
@CompileStatic
class LegalDocumentTools {
    @Tool("Returns the last time the PRIVACY document was updated") // <2>
    LocalDate lastUpdatePrivacy() {
        return LocalDate.of(2013, 3, 9) // <3>
    }
}
