package example.micronaut;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AssistantWithMemoryTest implements OllamaTestPropertyProvider {

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
