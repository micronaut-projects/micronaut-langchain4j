package example.micronaut.graal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Serves canned responses of the model providers' REST APIs, so that the providers' request and response mapping runs
 * in a native image without network access. Each provider has its own path prefix.
 */
final class FakeProviderServer implements AutoCloseable {

    private static final String OPENAI_CHAT = """
        {"id":"chatcmpl-1","object":"chat.completion","created":0,"model":"test",
         "choices":[{"index":0,"message":{"role":"assistant","content":"micronaut"},"finish_reason":"stop"}],
         "usage":{"prompt_tokens":3,"completion_tokens":1,"total_tokens":4}}
        """;
    private static final String OPENAI_EMBEDDINGS = """
        {"object":"list","model":"test","data":[{"object":"embedding","index":0,"embedding":[0.1,0.2,0.3]}],
         "usage":{"prompt_tokens":1,"total_tokens":1}}
        """;
    private static final String ANTHROPIC_MESSAGES = """
        {"id":"msg_1","type":"message","role":"assistant","model":"test",
         "content":[{"type":"text","text":"micronaut"}],"stop_reason":"end_turn",
         "usage":{"input_tokens":3,"output_tokens":1}}
        """;
    private static final String OLLAMA_CHAT = """
        {"model":"test","created_at":"2026-01-01T00:00:00Z","message":{"role":"assistant","content":"micronaut"},
         "done":true,"done_reason":"stop","prompt_eval_count":3,"eval_count":1}
        """;
    private static final String OLLAMA_EMBED = """
        {"model":"test","embeddings":[[0.1,0.2,0.3]]}
        """;
    private static final String GEMINI_GENERATE = """
        {"candidates":[{"content":{"role":"model","parts":[{"text":"micronaut"}]},"finishReason":"STOP"}],
         "usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":1,"totalTokenCount":4},"modelVersion":"test"}
        """;
    private static final String GEMINI_EMBED = """
        {"embedding":{"values":[0.1,0.2,0.3]}}
        """;
    private static final String GEMINI_BATCH_EMBED = """
        {"embeddings":[{"values":[0.1,0.2,0.3]}]}
        """;

    private static final String OPENAI_STREAM = """
        data: {"id":"c","object":"chat.completion.chunk","created":0,"model":"test","choices":[{"index":0,"delta":{"role":"assistant","content":"micro"},"finish_reason":null}]}

        data: {"id":"c","object":"chat.completion.chunk","created":0,"model":"test","choices":[{"index":0,"delta":{"content":"naut"},"finish_reason":"stop"}],"usage":{"prompt_tokens":3,"completion_tokens":1,"total_tokens":4}}

        data: [DONE]

        """;
    private static final String OLLAMA_STREAM = """
        {"model":"test","created_at":"2026-01-01T00:00:00Z","message":{"role":"assistant","content":"micro"},"done":false}
        {"model":"test","created_at":"2026-01-01T00:00:00Z","message":{"role":"assistant","content":"naut"},"done":true,"done_reason":"stop","prompt_eval_count":3,"eval_count":1}
        """;
    private static final String OLLAMA_TOOL_CALL = """
        {"model":"test","created_at":"2026-01-01T00:00:00Z","message":{"role":"assistant","content":"",
         "tool_calls":[{"function":{"name":"currentFramework","arguments":{}}}]},
         "done":true,"done_reason":"stop","prompt_eval_count":3,"eval_count":1}
        """;
    private static final String GEMINI_STREAM = """
        data: {"candidates":[{"content":{"role":"model","parts":[{"text":"micro"}]}}],"modelVersion":"test"}

        data: {"candidates":[{"content":{"role":"model","parts":[{"text":"naut"}]},"finishReason":"STOP"}],"usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":1,"totalTokenCount":4},"modelVersion":"test"}

        """;

    private static final String COHERE_RERANK = """
        {"id":"1","results":[{"index":1,"relevance_score":0.1},{"index":0,"relevance_score":0.9}],"meta":{"billed_units":{"search_units":1}}}
        """;
    private static final String COHERE_EMBED = """
        {"id":"1","texts":["Micronaut"],"embeddings":{"float":[[0.1,0.2,0.3]]},"meta":{"billed_units":{"input_tokens":1}}}
        """;
    private static final String JINA_RERANK = """
        {"model":"test","results":[{"index":1,"relevance_score":0.1},{"index":0,"relevance_score":0.9}],"usage":{"total_tokens":1}}
        """;
    private static final String VOYAGE_RERANK = """
        {"object":"list","data":[{"index":1,"relevance_score":0.1},{"index":0,"relevance_score":0.9}],"model":"test","usage":{"total_tokens":1}}
        """;

    private final HttpServer server;

    FakeProviderServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/", this::handle);
        server.start();
    }

    String url(String prefix) {
        return "http://localhost:" + server.getAddress().getPort() + "/" + prefix;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String compact = request.replaceAll("\\s", "");
        String accept = exchange.getRequestHeaders().getFirst("Accept");
        boolean stream = compact.contains("\"stream\":true") || accept != null && accept.contains("text/event-stream");
        String path = exchange.getRequestURI().getPath();
        String contentType = "application/json";
        String body;
        if (path.endsWith("rerank")) {
            body = path.contains("/cohere/") ? COHERE_RERANK : path.contains("/jina/") ? JINA_RERANK : VOYAGE_RERANK;
        } else if (path.contains("/cohere/") && path.endsWith("embed")) {
            body = COHERE_EMBED;
        } else         if (path.endsWith("/chat/completions")) {
            // OpenAI, Mistral AI and Azure OpenAI share the format
            body = stream ? OPENAI_STREAM : OPENAI_CHAT;
            contentType = stream ? "text/event-stream" : contentType;
        } else if (path.endsWith("/embeddings")) {
            body = OPENAI_EMBEDDINGS;
        } else if (path.endsWith("/messages")) {
            body = ANTHROPIC_MESSAGES;
        } else if (path.endsWith("/api/chat")) {
            // the tool calling test: call the tool first, then answer with its result
            boolean toolResult = compact.contains("\"role\":\"tool\"");
            if (compact.contains("\"tools\":[{") && !toolResult) {
                body = OLLAMA_TOOL_CALL;
            } else if (toolResult) {
                body = OLLAMA_CHAT.replace("micronaut", request.contains("Micronaut 5") ? "tool:Micronaut 5" : "tool:missing");
            } else {
                body = stream ? OLLAMA_STREAM : OLLAMA_CHAT;
                contentType = stream ? "application/x-ndjson" : contentType;
            }
        } else if (path.endsWith(":streamGenerateContent")) {
            body = GEMINI_STREAM;
            contentType = "text/event-stream";
        } else if (path.endsWith("/api/embed")) {
            body = OLLAMA_EMBED;
        } else if (path.endsWith(":generateContent")) {
            body = GEMINI_GENERATE;
        } else if (path.endsWith(":batchEmbedContents")) {
            body = GEMINI_BATCH_EMBED;
        } else if (path.endsWith(":embedContent")) {
            body = GEMINI_EMBED;
        } else {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
