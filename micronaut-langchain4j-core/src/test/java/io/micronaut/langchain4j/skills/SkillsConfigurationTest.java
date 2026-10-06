package io.micronaut.langchain4j.skills;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.skills.Skills;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.annotation.AiService;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillsConfigurationTest {

    static final String SPEC_NAME = "SkillsConfigurationTest";

    @TempDir
    Path directory;

    @Test
    void loadsTheSkillsOfADirectory() throws IOException {
        Path skill = Files.createDirectories(directory.resolve("translation"));
        Files.writeString(skill.resolve("SKILL.md"), """
            ---
            name: translation
            description: Translates travel phrases
            ---

            Translate the phrase, then give its pronunciation.
            """);
        try (ApplicationContext context = ApplicationContext.run(Map.of("langchain4j.skills.local.directory", directory.toString()))) {
            Skills skills = context.getBean(Skills.class, Qualifiers.byName("local"));
            assertTrue(skills.formatAvailableSkills().contains("translation"), skills.formatAvailableSkills());
        }
    }

    @Test
    void requiresSkills() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("langchain4j.skills.none.directory", directory.toString()))) {
            BeanInstantiationException error = assertThrows(BeanInstantiationException.class,
                () -> context.getBean(Skills.class, Qualifiers.byName("none")));
            assertTrue(error.getMessage().contains("No skills found for langchain4j.skills.none"), error.getMessage());
        }
    }

    @Test
    void listsTheSkillsAsTheSystemMessageOfAServiceWithoutOne() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.skills.travel.classpath", "skills"))) {
            String system = context.getBean(Guide.class).chat("Hello");
            assertTrue(system.startsWith("You have access to the following skills:"), system);
            assertTrue(system.contains("packing"), system);
        }
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService(skills = "travel")
    interface Guide {
        String chat(String message);
    }

    /**
     * Answers with the system message it receives.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class SystemMessageEchoModel implements ChatModel {
        @Override
        public ChatResponse doChat(ChatRequest request) {
            String system = request.messages().stream()
                .filter(SystemMessage.class::isInstance)
                .map(message -> ((SystemMessage) message).text())
                .findFirst()
                .orElse("");
            return ChatResponse.builder().aiMessage(AiMessage.from(system)).build();
        }
    }
}
