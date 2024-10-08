package io.micronaut.langchain4j.openai;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class OpenAiLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_OPEN_AI_API_KEY") != null &&
                System.getenv("LANGCHAIN4J_OPEN_AI_ORGANIZATION_ID") != null;
    }

}
