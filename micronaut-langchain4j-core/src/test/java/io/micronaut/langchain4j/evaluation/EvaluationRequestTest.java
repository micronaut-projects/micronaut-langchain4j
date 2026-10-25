package io.micronaut.langchain4j.evaluation;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvaluationRequestTest {

    @Test
    void createsRequestFromResultSources() {
        Result<String> result = Result.<String>builder()
            .content("Micronaut is a JVM framework.")
            .sources(List.of(
                Content.from("Micronaut is a JVM-based framework."),
                Content.from("It supports GraalVM native image.")
            ))
            .build();

        EvaluationRequest request = EvaluationRequest.from("What is Micronaut?", result);

        assertEquals("What is Micronaut?", request.userText());
        assertEquals("Micronaut is a JVM-based framework.\n\nIt supports GraalVM native image.", request.context());
        assertEquals("Micronaut is a JVM framework.", request.response());
    }

    @Test
    void leavesContextNullWhenNoSourcesExist() {
        EvaluationRequest request = EvaluationRequest.from("Hello", List.of(), "Hi");

        assertNull(request.context());
    }

    @Test
    void rejectsBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> new EvaluationRequest(" ", null, "response"));
        assertThrows(IllegalArgumentException.class, () -> new EvaluationRequest("prompt", null, " "));
    }
}
