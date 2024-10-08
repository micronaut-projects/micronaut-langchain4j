package io.micronaut.langchain4j.vertexai.gemini;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck")
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Vertex AI Gemini implementation")
public class VertexAiGeminiSuite {
}
