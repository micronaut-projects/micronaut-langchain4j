package io.micronaut.langchain4j.testutils;

import io.micronaut.context.exceptions.ConfigurationException;
import org.jspecify.annotations.NonNull;
import io.micronaut.test.support.TestPropertyProvider;

import java.util.HashMap;
import java.util.Map;

public interface OllamaTestPropertyProvider extends TestPropertyProvider {
    @Override
    @NonNull
    default Map<String, String> getProperties() {
        Map<String, String> result = new HashMap<>();
        try {
        } catch (Exception e) {
            throw new ConfigurationException("Could not set Ollama test properties", e);
        }
        return result;
    }
}
