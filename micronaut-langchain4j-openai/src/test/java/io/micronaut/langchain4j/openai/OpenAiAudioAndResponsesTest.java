package io.micronaut.langchain4j.openai;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.model.audio.AudioTranscriptionModel;
import dev.langchain4j.model.audio.AudioTranscriptionRequest;
import dev.langchain4j.model.audio.TextToSpeechModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiResponsesChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The audio transcription, text-to-speech and Responses API models, against a fake OpenAI endpoint.
 */
class OpenAiAudioAndResponsesTest {

    private static final String RESPONSE = """
        {"id":"resp_1","object":"response","created_at":0,"status":"completed","model":"test",
         "output":[{"type":"message","id":"msg_1","status":"completed","role":"assistant",
                    "content":[{"type":"output_text","text":"micronaut","annotations":[]}]}],
         "usage":{"input_tokens":3,"output_tokens":1,"total_tokens":4}}
        """;

    private final List<String> paths = new CopyOnWriteArrayList<>();
    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path = exchange.getRequestURI().getPath();
            paths.add(path);
            byte[] response;
            String contentType = "application/json";
            if (path.endsWith("/audio/transcriptions")) {
                response = "{\"text\":\"micronaut\"}".getBytes(StandardCharsets.UTF_8);
            } else if (path.endsWith("/audio/speech")) {
                response = new byte[] {1, 2, 3};
                contentType = "audio/mpeg";
            } else {
                response = RESPONSE.getBytes(StandardCharsets.UTF_8);
            }
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private ApplicationContext context(Map<String, Object> models) {
        Map<String, Object> properties = new java.util.HashMap<>(models);
        properties.put("langchain4j.open-ai.api-key", "test");
        properties.put("langchain4j.open-ai.base-url", "http://localhost:" + server.getAddress().getPort() + "/");
        return ApplicationContext.run(properties);
    }

    @Test
    void theAudioAndResponsesModelsAreOptIn() {
        try (ApplicationContext context = context(Map.of())) {
            assertFalse(context.containsBean(AudioTranscriptionModel.class));
            assertFalse(context.containsBean(TextToSpeechModel.class));
            assertFalse(context.containsBean(OpenAiResponsesChatModel.class));
        }
    }

    @Test
    void transcribesAudio() {
        try (ApplicationContext context = context(Map.of("langchain4j.open-ai.audio-transcription-model.model-name", "test"))) {
            String text = context.getBean(AudioTranscriptionModel.class).transcribe(AudioTranscriptionRequest.builder()
                .audio(Audio.builder().binaryData(new byte[] {1, 2, 3}).mimeType("audio/mpeg").build())
                .build()).text();
            assertEquals("micronaut", text);
            assertTrue(paths.contains("/audio/transcriptions"), paths::toString);
        }
    }

    @Test
    void synthesizesSpeech() {
        try (ApplicationContext context = context(Map.of(
            "langchain4j.open-ai.text-to-speech-model.model-name", "test",
            "langchain4j.open-ai.text-to-speech-model.voice", "alloy"))) {
            Audio audio = context.getBean(TextToSpeechModel.class).synthesize("Micronaut").audio();
            assertEquals(3, audio.binaryData().length);
        }
    }

    @Test
    void chatsWithTheResponsesApi() {
        // the Responses client appends "/responses" to the base URL: it must not end with a slash
        try (ApplicationContext context = context(Map.of(
            "langchain4j.open-ai-responses.chat-models.responses.model-name", "test",
            "langchain4j.open-ai-responses.chat-models.responses.base-url", "http://localhost:" + server.getAddress().getPort()))) {
            ChatModel model = context.getBean(ChatModel.class, Qualifiers.byName("responses"));
            assertInstanceOf(OpenAiResponsesChatModel.class, model);
            assertEquals("micronaut", model.chat("Which framework?"));
            assertTrue(paths.contains("/responses"), paths::toString);
        }
    }
}
