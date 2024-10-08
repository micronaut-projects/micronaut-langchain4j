package io.micronaut.langchain4j.vertexai.gemini;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class VertexAiGeminiLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_VERTEX_AI_GEMINI_MODEL_NAME") != null
                && System.getenv("LANGCHAIN4J_VERTEX_AI_GEMINI_PROJECT") != null
                && System.getenv("LANGCHAIN4J_VERTEX_AI_GEMINI_LOCATION") != null;
    }

}
