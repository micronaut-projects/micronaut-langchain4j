plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(projects.micronautLangchain4jCore)
    api(mnTest.junit.jupiter.api)
    api(mnTest.junit.jupiter.params)
    implementation(mn.snakeyaml)
    testAnnotationProcessor(projects.micronautLangchain4jProcessor)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnLogging.logback.classic)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
