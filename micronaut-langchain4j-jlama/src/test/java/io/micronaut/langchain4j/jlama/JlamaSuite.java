package io.micronaut.langchain4j.jlama;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.langchain4j.chatmodels.tck.tests")
@SuiteDisplayName("ChatModels Test Compatibility Kit for the Jlama implementation")
public class JlamaSuite {
}
