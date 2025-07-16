package example.micronaut;

import dev.langchain4j.memory.ChatMemory;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.langchain4j.testutils.OllamaUtils;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AssistantWithMemoryTest implements TestPropertyProvider {
    @Override
    public @NonNull Map<String, String> getProperties() {
        Map<String, String> result = new HashMap<>();
        try {
            result.put("langchain4j.ollama.base-url", OllamaUtils.ollamaContainerBaseUrl());
        } catch (Exception e) {
            throw new ConfigurationException("Could not set Ollama base URL", e);
        }
        return result;
    }

    //tag::test[]
    @Test
    void chatWithMemory(AssistantWithMemory assistant) {
        MemoryIdAndResponse johnConversation = assistant.chat("Let me introduce myself. My name is John");
        String johnConversationId = johnConversation.memoryId();
        assertNotNull(johnConversationId);
        MemoryIdAndResponse aegonConversation = assistant.chat("Let me introduce myself. My name is Dan");
        String aegonConversationId = aegonConversation.memoryId();
        assertNotNull(aegonConversationId);
        MemoryIdAndResponse answer = assistant.chat(johnConversationId, "What's my name?");
        assertTrue(answer.response().toLowerCase().contains("john"), answer.response());
        answer = assistant.chat(aegonConversationId, "What's my name?");
        assertTrue(answer.response().toLowerCase().contains("dan"), answer.response());
    }
    //end::test[]
}
