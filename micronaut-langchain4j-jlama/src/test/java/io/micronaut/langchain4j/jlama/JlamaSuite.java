package io.micronaut.langchain4j.jlama;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck.tests")
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Jlama implementation")
@ExcludeClassNamePatterns({
        "io.micronaut.langchain4j.chatmodels.tck.tests.AiServiceTest",
        "io.micronaut.langchain4j.chatmodels.tck.tests.ChatLanguageModelTest"
})
// Flaky failures Unexpected exception thrown: java.lang.ArrayIndexOutOfBoundsException: Index 65536 out of bounds for length 65536
public class JlamaSuite {
}
