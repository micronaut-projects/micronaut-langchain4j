plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    testAnnotationProcessor(mn.micronaut.inject.java)
    testAnnotationProcessor(projects.micronautLangchain4jProcessor)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(projects.micronautLangchain4jCore)
    testImplementation(libs.micronaut.mcp.client.langchain4j)
    testImplementation(libs.micronaut.mcp.server.java.sdk)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mn.micronaut.http.client)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}
tasks.withType<Test> {
    useJUnitPlatform()
}
