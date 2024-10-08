package io.micronaut.langchain4j.vertexai;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class VertexAiLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_VERTEX_AI_ENDPOINT") != null
                && System.getenv("LANGCHAIN4J_VERTEX_AI_PROJECT") != null
                && System.getenv("LANGCHAIN4J_VERTEX_AI_LOCATION") != null;
    }

}
