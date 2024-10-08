package io.micronaut.langchain4j.chatmodels.tck;

public class DefaultLangchain4jProviderEnabled implements Langchain4jProviderEnabled {
    @Override
    public boolean isEnabled() {
        return true;
    }
}
