plugins {
    `java-library`
    groovy
}
repositories {
    mavenCentral()
}
dependencies {
    compileOnly(mn.micronaut.inject.groovy)
    implementation(mnGroovy.micronaut.runtime.groovy)
    compileOnly(project(":micronaut-langchain4j-processor"))
    compileOnly(mnJsonSchema.micronaut.json.schema.processor)
    implementation(project(":micronaut-langchain4j-core"))
    implementation(mn.reactive.streams)
    implementation(mnJsonSchema.micronaut.json.schema.utils)
    implementation(project(":micronaut-langchain4j-agentic"))
    implementation(project(":micronaut-langchain4j-openai"))
    implementation(project(":micronaut-langchain4j-google-genai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))

    testImplementation(project(":test-suite-utils"))
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.platform.launcher)

    testRuntimeOnly(mn.micronaut.http.client)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
tasks.withType<Test> {
    useJUnitPlatform()
}
