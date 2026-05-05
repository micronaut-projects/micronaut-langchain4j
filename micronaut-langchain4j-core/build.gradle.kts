plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(mn.micronaut.context)
    api(libs.langchain4j)
    api(libs.langchain4j.http.client)
    implementation(mn.micronaut.http.client)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}
