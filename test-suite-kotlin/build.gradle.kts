plugins {
    id ("io.micronaut.build.internal.kotlin-ksp")
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    ksp(mn.micronaut.inject.kotlin)
    kspTest(mn.micronaut.inject.kotlin)
    implementation(mnKotlin.micronaut.kotlin.runtime)
    ksp(project(":micronaut-langchain4j-processor"))
    ksp(mnJsonSchema.micronaut.json.schema.processor)
    kspTest(project(":micronaut-langchain4j-processor"))
    implementation(project(":micronaut-langchain4j-core"))
    implementation(mn.reactive.streams)
    implementation(mn.micronaut.http.client.core)
    implementation(mnJsonSchema.micronaut.json.schema.utils)
    implementation(project(":micronaut-langchain4j-agentic"))
    implementation(libs.langchain4j.agentic.mcp)
    implementation(project(":micronaut-langchain4j-openai"))
    implementation(project(":micronaut-langchain4j-google-genai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))
    testImplementation(project(":test-suite-utils"))
    testImplementation(project(":micronaut-langchain4j-test"))
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(platform(libs.boms.langchain4j))
    testImplementation(libs.langchain4j.kotlin)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mn.micronaut.http.client)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(mnSerde.micronaut.serde.jackson)

    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}
tasks.withType<Test> {
    useJUnitPlatform()
}
