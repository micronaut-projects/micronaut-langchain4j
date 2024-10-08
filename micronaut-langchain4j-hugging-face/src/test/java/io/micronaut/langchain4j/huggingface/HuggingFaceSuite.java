package io.micronaut.langchain4j.huggingface;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck.tests")
@ExcludeClassNamePatterns({
    "io.micronaut.langchain4j.chatmodels.tck.tests.AiServiceTest" // fails with error {"error":"Cannot override task for LLM models"}
}
)
@SuiteDisplayName("ChatModels Test Compatibility Kit for the HuggingFace implementation")
public class HuggingFaceSuite {
}
