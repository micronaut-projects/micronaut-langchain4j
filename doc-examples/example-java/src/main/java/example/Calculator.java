package example;

import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class Calculator {
    @Tool
    public double add(int a, int b) {
        return a + b;
    }

    @Tool
    public double squareRoot(double x) {
        return Math.sqrt(x);
    }
}
