package io.micronaut.langchain4j.huggingface;

import io.micronaut.langchain4j.chatmodels.tck.Langchain4jProviderEnabled;

public class HuggingFaceLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return System.getenv("LANGCHAIN4J_HUGGING_FACE_ACCESS_TOKEN") != null;
    }
}
