package example.micronaut.aiservice.tools

import io.micronaut.langchain4j.annotation.AiService

@AiService(tools = LegalDocumentTools.class)
interface CompanyBot {
    String ask(String question)
}
