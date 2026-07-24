plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(mn.micronaut.context)
    api(libs.langchain4j)
    api(libs.langchain4j.http.client)
    implementation(platform(mnReactor.boms.reactor))
    implementation(mn.micronaut.http.client.core)
    implementation(libs.reactor.core)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mn.micronaut.http.client)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}
