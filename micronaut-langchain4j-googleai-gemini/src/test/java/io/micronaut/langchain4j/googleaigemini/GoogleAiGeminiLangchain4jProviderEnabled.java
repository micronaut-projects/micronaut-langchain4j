package io.micronaut.langchain4j.googleaigemini;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class GoogleAiGeminiLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_GOOGLE_AI_GEMINI_API_KEY") != null;
    }
}
