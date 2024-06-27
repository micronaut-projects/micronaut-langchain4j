package example.openai;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(
    name = "langchain4j.open-ai.embedding-model.model-name",
    value = "text-embedding-ada-002"
)
public class ToolTest {
    @Inject
    ApplicationContext applicationContext;

    @Test
    void testInjectTools() {
        MathGenius mathGenius = applicationContext.getBean(MathGenius.class);
        Assertions.assertNotNull(mathGenius);
        String result = mathGenius.ask("What is the square root of 475695037565?");
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.startsWith("The square root of 475695037565 is"));
    }
}
