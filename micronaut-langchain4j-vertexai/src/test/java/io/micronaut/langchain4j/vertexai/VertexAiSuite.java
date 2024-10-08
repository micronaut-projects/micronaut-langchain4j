package io.micronaut.langchain4j.vertexai;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck.tests")
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Vertex AI PaLM 2 implementation")
@ExcludeClassNamePatterns({
        "io.micronaut.langchain4j.chatmodels.tck.tests.AiServiceTest"  // com.google.api.gax.rpc.InvalidArgumentException: io.grpc.StatusRuntimeException: INVALID_ARGUMENT: Invalid instance. Missing required messages field. fields 	 {key: "content"value {string_value: "Hello"}}
})
public class VertexAiSuite {
}
