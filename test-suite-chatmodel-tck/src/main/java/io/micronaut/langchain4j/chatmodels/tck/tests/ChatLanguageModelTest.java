package io.micronaut.langchain4j.chatmodels.tck.tests;

import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.chatmodels.tck.SuiteCondition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Requires(condition = SuiteCondition.class)
@MicronautTest(startApplication = false)
class ChatLanguageModelTest {
    private static final Logger LOG = LoggerFactory.getLogger(ChatLanguageModelTest.class);

    @Inject
    List<JokeGenerator> jokeGenerators;

    @Test
    void testJokeGeneration() {
        for (JokeGenerator jokeGenerator : jokeGenerators) {
            String joke = assertDoesNotThrow(jokeGenerator::generateJoke);
            LOG.trace("{}", joke);
            assertTrue(joke.contains("Java"));
        }
    }
}
