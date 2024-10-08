package io.micronaut.langchain4j.googleaigemini;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck")
@ExcludeClassNamePatterns({
        "io.micronaut.langchain4j.chatmodels.tck.tests.StreamingChatLanguageModelTest", // no bean of type StreamingChatLanguage. No streaming chat model is available yet for Google AI Gemini in langchain4j
})
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Google AI Gemini implementation")
public class GoogleAiGeminiSuite {
}
