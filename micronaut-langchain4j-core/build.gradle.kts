plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(platform(libs.boms.langchain4j))
    api(mn.micronaut.context)
    api(libs.langchain4j)
    compileOnly(libs.langchain4j.ollama)
    compileOnly(libs.langchain4j.open.ai)
    compileOnly(libs.langchain4j.bedrock)
    compileOnly(libs.langchain4j.anthropic)
    compileOnly(libs.langchain4j.mistral.ai)
    compileOnly(libs.langchain4j.vertex.ai.gemini)
    compileOnly(libs.langchain4j.vertex.ai)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(projects.micronautLangchain4jOllamaTestresource)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
