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
            SupplierParameterResolver.Context binding = context(method, parameters[1]);

            assertFalse(resolver.supports(context(method, parameters[1])), "not creating an agent");

            BeanContextSupplierParameterResolver.creating(context, () -> {
                assertFalse(resolver.supports(context(method, parameters[0])), "a java.lang type is not a bean");
                assertFalse(resolver.supports(context(method, parameters[2])), "@V reads the agentic scope");
                assertFalse(resolver.supports(context(method, parameters[3])), "no bean of the type");
                assertTrue(resolver.supports(binding));
                // a nested creation restores the outer one
                BeanContextSupplierParameterResolver.creating(context, () -> assertSame(service, resolver.resolve(binding)));
                assertSame(service, resolver.resolve(binding));
            });

            // outside of the creation, the context that created the agent resolves the parameters
            assertSame(service, resolver.resolve(binding));

            BeanContextSupplierParameterResolver.release(context);
            SupplierParameterResolver.Context released = binding;
            IllegalStateException error = assertThrows(IllegalStateException.class, () -> resolver.resolve(released));
            assertEquals("The bean context that created the agent " + Suppliers.class.getName() + " is closed", error.getMessage());
        }
    }

    @Test
    void resolvesTheParametersFromTheContextThatCreatedEachAgent() throws NoSuchMethodException {
        Method method = Suppliers.class.getDeclaredMethod("supply", String.class, Service.class, Service.class, Unknown.class);
        Parameter parameter = method.getParameters()[1];
        SupplierParameterResolver resolver = resolver();
        try (ApplicationContext first = ApplicationContext.run(); ApplicationContext second = ApplicationContext.run()) {
            Service firstService = new Service();
            Service secondService = new Service();
            first.registerSingleton(Service.class, firstService);
            second.registerSingleton(Service.class, secondService);

            // the same agent type created by two live contexts, each with its own binding of the parameter
            SupplierParameterResolver.Context firstBinding = context(method, parameter);
            SupplierParameterResolver.Context secondBinding = context(method, parameter);
            BeanContextSupplierParameterResolver.creating(first, () -> assertTrue(resolver.supports(firstBinding)));
            BeanContextSupplierParameterResolver.creating(second, () -> assertTrue(resolver.supports(secondBinding)));

            assertSame(firstService, resolver.resolve(firstBinding));
            assertSame(secondService, resolver.resolve(secondBinding));

            BeanContextSupplierParameterResolver.release(second);
            assertSame(firstService, resolver.resolve(firstBinding));
            assertThrows(IllegalStateException.class, () -> resolver.resolve(secondBinding));
            BeanContextSupplierParameterResolver.release(first);
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
            return java.util.Arrays.asList(text, service, fromScope, unknown);
        }
    }
}
