package example.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.langchain4j.testutils.OllamaUtils;

import java.util.Map;

/**
 * Supplies the base URL of the shared Ollama test container to the Python tests run with the
 * {@code ollama} environment, like {@code OllamaTestPropertyProvider} does for the Java, Kotlin and Groovy suites.
 * <p>
 * The configurer is written in Java because Micronaut Test calls {@code TestPropertyProvider.getProperties()}
 * before the application context, and with it the GraalPy runtime, exists, so a Python test class cannot
 * implement {@code OllamaTestPropertyProvider}. It uses the {@link #configure(ApplicationContext)} callback
 * because the {@link io.micronaut.context.ApplicationContextBuilder} is configured before {@code @MicronautTest}
 * selects the environments, so the {@code ollama} environment can only be checked on the built context.
 */
@ContextConfigurer
public class OllamaTestConfigurer implements ApplicationContextConfigurer {

    public static final String OLLAMA_ENVIRONMENT = "ollama";

    @Override
    public void configure(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (environment.getActiveNames().contains(OLLAMA_ENVIRONMENT)) {
            try {
                environment.addPropertySource(PropertySource.of(OLLAMA_ENVIRONMENT, Map.of(
                    "langchain4j.ollama.base-url", OllamaUtils.ollamaContainerBaseUrl(),
                    "langchain4j.ollama.embedding-model.model-name", OllamaUtils.ollamaEmbeddingModelName()
                )));
            } catch (Exception e) {
                throw new ConfigurationException("Could not set Ollama base URL", e);
            }
        }
    }
}
