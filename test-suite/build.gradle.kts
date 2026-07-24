plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(projects.micronautLangchain4jProcessor)
    implementation(projects.micronautLangchain4jCore)
    implementation(projects.micronautLangchain4jAgentic)
    implementation(projects.micronautLangchain4jOpenai)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(projects.micronautLangchain4jOllama)

    testImplementation(projects.testSuiteUtils)
    testImplementation(projects.micronautLangchain4jStoreRedis)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(projects.micronautLangchain4jStoreNeo4j)
    testImplementation(projects.micronautLangchain4jCassandra)
    testImplementation(mnTest.micronaut.test.junit5)
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
