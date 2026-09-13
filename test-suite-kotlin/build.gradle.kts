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
    kspTest(project(":micronaut-langchain4j-processor"))
    implementation(project(":micronaut-langchain4j-core"))
    implementation(project(":micronaut-langchain4j-agentic"))
    implementation(project(":micronaut-langchain4j-openai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))
    testImplementation(project(":test-suite-utils"))
    testImplementation(mnTest.micronaut.test.junit5)
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
