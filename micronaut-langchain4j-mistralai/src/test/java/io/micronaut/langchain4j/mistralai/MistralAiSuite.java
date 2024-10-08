package io.micronaut.langchain4j.mistralai;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck.tests")
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Mistral AI implementation")
@ExcludeClassNamePatterns({
        "io.micronaut.langchain4j.chatmodels.tck.tests.AiServiceTest", // status code: 400; body: {"object":"error","message":"Invalid model: mistral-tiny","type":"invalid_model","param":null,"code":"1500"}
})
public class MistralAiSuite {
}
