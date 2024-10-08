package io.micronaut.langchain4j.mistralai;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class MistralAiLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_MISTRAL_AI_API_KEY") != null;
    }

}
