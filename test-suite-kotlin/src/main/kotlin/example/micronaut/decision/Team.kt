package example.micronaut.decision

import dev.langchain4j.model.output.structured.Description

enum class Team {
    @Description("Payments, invoices and refunds") // <1>
    BILLING,
    @Description("Problems using the product, crashes and errors")
    SUPPORT
}
