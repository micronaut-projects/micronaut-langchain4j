package io.micronaut.langchain4j.ollama;

import io.micronaut.langchain4j.testutils.ReflectionMetadata;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the native image metadata of the module registers every request and response type LangChain4j maps
 * with Jackson. Run with the environment variable {@code UPDATE_NATIVE_METADATA=true} to rewrite it.
 */
class NativeImageMetadataTest {

    @Test
    void registersTheRequestAndResponseTypes() {
        ReflectionMetadata.verify("micronaut-langchain4j-ollama", dev.langchain4j.model.ollama.OllamaChatModel.class, "dev.langchain4j.model.ollama");
    }
}
