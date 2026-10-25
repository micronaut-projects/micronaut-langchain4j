package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Langchain4jConfigVisitorBehaviorTest extends AbstractTypeElementSpec {

    @Test
    void namedDefaultConfigurationBecomesThePrimaryQualifiedBean() throws ClassNotFoundException {
        try (ApplicationContext context = buildContext("test.TestModule", """
            package test;

            import static io.micronaut.langchain4j.annotation.Lang4jConfig.*;

            import io.micronaut.langchain4j.annotation.Lang4jConfig;

            @Lang4jConfig(
                models = @Model(
                    kind = StubKind.class,
                    impl = StubModel.class,
                    defaultModelName = "fallback-model"
                ),
                properties = @Property(name = "modelName", required = true)
            )
            final class TestModule {
            }

            interface StubKind {
                String getModelName();
            }

            final class StubModel implements StubKind {
                private final String modelName;

                StubModel(String modelName) {
                    this.modelName = modelName;
                }

                @Override
                public String getModelName() {
                    return modelName;
                }

                static Builder builder() {
                    return new Builder();
                }

                static final class Builder {
                    private String modelName;

                    Builder modelName(String modelName) {
                        this.modelName = modelName;
                        return this;
                    }

                    StubModel build() {
                        return new StubModel(modelName);
                    }
                }
            }
            """, false, Map.of(
            "langchain4j.stub-model.stub-kind.model-name", "singular-default",
            "langchain4j.stub-model.stub-kinds.default.model-name", "named-default",
            "langchain4j.stub-model.stub-kinds.pirate.model-name", "pirate-model"
        ))) {
            Class<?> builderType = context.getClassLoader().loadClass("test.StubModel$Builder");
            Class<?> modelType = context.getClassLoader().loadClass("test.StubKind");

            assertEquals(2, context.getBeansOfType(builderType).size());
            assertEquals(1, context.getBeansOfType(builderType, Qualifiers.byName("default")).size());
            assertEquals(1, context.getBeansOfType(builderType, Qualifiers.byName("pirate")).size());
            assertEquals(2, context.getBeansOfType(modelType).size());
            assertEquals(1, context.getBeansOfType(modelType, Qualifiers.byName("default")).size());
            assertEquals(1, context.getBeansOfType(modelType, Qualifiers.byName("pirate")).size());
            assertEquals("default", modelName(context.getBean(modelType)));
            assertEquals("default", modelName(context.getBean(modelType, Qualifiers.byName("default"))));
            assertEquals("pirate", modelName(context.getBean(modelType, Qualifiers.byName("pirate"))));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object modelName(Object bean) throws ReflectiveOperationException {
        Method method = bean.getClass().getDeclaredMethod("getModelName");
        method.setAccessible(true);
        return method.invoke(bean);
    }
}
