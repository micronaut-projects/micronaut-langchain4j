from dev.langchain4j.agent.tool import Tool
from jakarta.inject import Singleton
from java.time import LocalDate


@Singleton  # <1>
class LegalDocumentTools:
    @Tool("Returns the last time the PRIVACY document was updated")  # <2>
    def last_update_privacy(self) -> LocalDate:
        return LocalDate.of(2013, 3, 9)  # <3>
