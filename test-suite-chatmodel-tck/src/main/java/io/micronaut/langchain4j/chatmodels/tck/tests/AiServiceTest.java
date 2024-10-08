package io.micronaut.langchain4j.chatmodels.tck.tests;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.chatmodels.tck.SuiteCondition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Requires(condition = SuiteCondition.class)
@Property(name = "spec.name", value = "AiServiceTest")
@MicronautTest(startApplication = false)
public class AiServiceTest {
    @Test
    void testAiService(Friend friend) {
        String result = assertDoesNotThrow(() -> friend.chat("Hello"));
        Assertions.assertNotNull(result);
    }
}
