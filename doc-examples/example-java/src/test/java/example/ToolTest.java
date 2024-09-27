package example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest
@Disabled("Ollama Testcontainers broken?")
public class ToolTest {
    @Inject
    ApplicationContext applicationContext;

    @Test
    void testInjectTools() {
        MathGenius mathGenius = applicationContext.getBean(MathGenius.class);
        Assertions.assertNotNull(mathGenius);
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () ->
            mathGenius.ask("whatever")
        );
        Assertions.assertEquals("Tools are currently not supported by this model", e.getMessage());
    }
}
