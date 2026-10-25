package io.micronaut.langchain4j.chatmodels.tck.tests;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.chatmodels.tck.SuiteCondition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Requires(condition = SuiteCondition.class)
@Property(name = "spec.name", value = "AiServiceFluxTest")
@MicronautTest(startApplication = false)
public class AiServiceFluxTest {
    @Test
    void testAiServiceWithFluxReturn(FluxFriend friend) {

        Flux<String> response = friend.chat("Tell me a story of 500 words");

        StepVerifier.create(response)
            .expectNextMatches(StringUtils::isNotEmpty)
            .thenConsumeWhile(chunk -> true)
            .verifyComplete();
    }
}
