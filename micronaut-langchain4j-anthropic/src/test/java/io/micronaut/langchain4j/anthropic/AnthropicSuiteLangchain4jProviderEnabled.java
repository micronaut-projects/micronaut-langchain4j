package io.micronaut.langchain4j.anthropic;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class AnthropicSuiteLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_ANTHROPIC_API_KEY") != null;
    }
}
