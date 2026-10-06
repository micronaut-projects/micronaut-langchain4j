package io.micronaut.langchain4j.agentic;

import dev.langchain4j.agentic.declarative.SupplierParameterResolver;
import dev.langchain4j.service.V;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which supplier method parameters the bean context resolves.
 */
class BeanContextSupplierParameterResolverTest {

    @Test
    void onlyResolvesBeanParametersWhileCreatingAnAgent() throws NoSuchMethodException {
        Method method = Suppliers.class.getDeclaredMethod("supply", String.class, Service.class, Service.class, Unknown.class);
        Parameter[] parameters = method.getParameters();
        SupplierParameterResolver resolver = resolver();
        try (ApplicationContext context = ApplicationContext.run()) {
            Service service = new Service();
            context.registerSingleton(Service.class, service);

            assertFalse(resolver.supports(context(method, parameters[1])), "not creating an agent");

            BeanContextSupplierParameterResolver.creating(context, () -> {
                assertFalse(resolver.supports(context(method, parameters[0])), "a java.lang type is not a bean");
                assertFalse(resolver.supports(context(method, parameters[2])), "@V reads the agentic scope");
                assertFalse(resolver.supports(context(method, parameters[3])), "no bean of the type");
                assertTrue(resolver.supports(context(method, parameters[1])));
                // a nested creation restores the outer one
                BeanContextSupplierParameterResolver.creating(context, () -> assertSame(service, resolver.resolve(context(method, parameters[1]))));
                assertSame(service, resolver.resolve(context(method, parameters[1])));
            });

            // outside of the creation, the context that created the agent type resolves the parameters
            assertSame(service, resolver.resolve(context(method, parameters[1])));

            BeanContextSupplierParameterResolver.release(context);
            IllegalStateException error = assertThrows(IllegalStateException.class, () -> resolver.resolve(context(method, parameters[1])));
            assertEquals("The bean context that created the agent " + Suppliers.class.getName() + " is closed", error.getMessage());
        }
    }

    private static SupplierParameterResolver resolver() {
        // the resolver registers itself when its class is initialized
        BeanContextSupplierParameterResolver.release(null);
        return dev.langchain4j.agentic.declarative.DeclarativeUtil.getSupplierParameterResolvers().stream()
            .filter(BeanContextSupplierParameterResolver.class::isInstance)
            .findFirst()
            .orElseThrow();
    }

    private static SupplierParameterResolver.Context context(Method method, Parameter parameter) {
        return new SupplierParameterResolver.Context() {
            @Override
            public Class<?> declaringAgentClass() {
                return Suppliers.class;
            }

            @Override
            public Method supplierMethod() {
                return method;
            }

            @Override
            public Parameter parameter() {
                return parameter;
            }
        };
    }

    static final class Service {
    }

    static final class Unknown {
    }

    interface Suppliers {
        static Object supply(String text, Service service, @V("service") Service fromScope, Unknown unknown) {
            return service;
        }
    }
}
