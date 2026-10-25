package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec;
import io.micronaut.core.annotation.AllowsReflection;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.ElementQuery;
import io.micronaut.inject.ast.MethodElement;
import org.junit.jupiter.api.Test;

/**
 * The LangChain4j annotations read reflectively, and the annotations of the integration, imply the
 * {@link AllowsReflection} hint, so that the Python compiler copies them onto the generated Java types.
 */
class AllowsReflectionRemapperTest extends AbstractTypeElementSpec {

    @Test
    void aiServiceImpliesAllowsReflection() {
        ClassElement element = buildClassElement("""
            package test;

            import dev.langchain4j.service.SystemMessage;
            import dev.langchain4j.service.UserMessage;
            import dev.langchain4j.service.V;
            import io.micronaut.langchain4j.annotation.AiService;

            @AiService
            interface Friend {
                @SystemMessage("You are a friend")
                @UserMessage("Say hi to {{name}}")
                String chat(@V("name") String name);
            }
            """);
        assertTrue(element.hasStereotype(AllowsReflection.class));
        MethodElement chat = method(element, "chat");
        assertTrue(chat.hasStereotype(AllowsReflection.class));
        assertTrue(chat.getParameters()[0].hasStereotype(AllowsReflection.class));
    }

    @Test
    void agenticAnnotationsImplyAllowsReflection() {
        ClassElement element = buildClassElement("""
            package test;

            import dev.langchain4j.agentic.Agent;
            import dev.langchain4j.agentic.declarative.LoopAgent;
            import dev.langchain4j.service.V;

            interface Translator {
                @Agent(outputKey = "text")
                String translate(@V("text") String text);

                @LoopAgent(subAgents = Translator.class, outputKey = "text", maxIterations = 3)
                String loop(@V("text") String text);
            }
            """);
        assertTrue(method(element, "translate").hasStereotype(AllowsReflection.class));
        assertTrue(method(element, "loop").hasStereotype(AllowsReflection.class));
    }

    @Test
    void agenticServiceImpliesAllowsReflection() {
        ClassElement element = buildClassElement("""
            package test;

            import io.micronaut.langchain4j.agentic.annotation.AgenticService;

            @AgenticService
            interface Greeter {
                String greet(String name);
            }
            """);
        assertTrue(element.hasStereotype(AllowsReflection.class));
    }

    @Test
    void toolImpliesAllowsReflection() {
        ClassElement element = buildClassElement("""
            package test;

            import dev.langchain4j.agent.tool.P;
            import dev.langchain4j.agent.tool.Tool;

            class Tools {
                @Tool("Adds two numbers")
                int add(@P("first") int a, @P("second") int b) {
                    return a + b;
                }
            }
            """);
        MethodElement add = method(element, "add");
        assertTrue(add.hasStereotype(AllowsReflection.class));
        assertTrue(add.getParameters()[0].hasStereotype(AllowsReflection.class));
    }

    private static MethodElement method(ClassElement element, String name) {
        return element.getEnclosedElement(ElementQuery.ALL_METHODS.named(name)).orElseThrow();
    }
}
