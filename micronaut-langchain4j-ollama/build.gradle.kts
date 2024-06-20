plugins {
    id("io.micronaut.build.internal.langchain4j-module")
    id("io.micronaut.test-resources")
}

dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSourcegen.micronaut.sourcegen.generator.java)
    api(projects.micronautLangchain4jCore)
    api(libs.langchain4j.ollama)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
    testResourcesService(projects.micronautLangchain4jOllamaTestresource)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
