package io.micronaut.langchain4j.ollama;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck")
@ExcludeClassNamePatterns("io.micronaut.langchain4j.chatmodels.tck.AiServiceTest")
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Ollama implementation")
public class OllamaSuiteTest {
}
