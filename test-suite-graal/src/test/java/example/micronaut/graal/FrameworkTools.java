package example.micronaut.graal;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class FrameworkTools {
    @Tool("Returns the framework in use")
    public String currentFramework() {
        return "Micronaut 5";
    }
}
