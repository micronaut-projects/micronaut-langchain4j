package example.micronaut.decision;

import dev.langchain4j.model.output.structured.Description;

public enum Team {
    @Description("Payments, invoices and refunds") // <1>
    BILLING,
    @Description("Problems using the product, crashes and errors")
    SUPPORT
}
