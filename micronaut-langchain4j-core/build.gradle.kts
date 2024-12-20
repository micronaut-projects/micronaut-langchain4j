plugins {
    id("io.micronaut.build.internal.langchain4j-module")
    id("io.micronaut.test-resources")
//    id("org.graalvm.buildtools.native")
}

dependencies {
    api(mn.micronaut.context)
    api(libs.langchain4j)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
//    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testResourcesService(projects.micronautLangchain4jOllamaTestresource)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}

micronaut {
    version(libs.versions.micronaut.platform.get())
}
