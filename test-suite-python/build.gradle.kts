plugins {
    `java-library`
    id("io.micronaut.build.internal.python")
}
repositories {
    mavenCentral()
}
dependencies {
    // The main Python sources (the `source="main"` snippets) are compiled by the Python compiler
    // (micronaut-inject-python), which takes the compile classpath as its annotation processor path: the
    // annotation processors are regular dependencies rather than annotation processor ones.
    implementation(projects.micronautLangchain4jProcessor)
    implementation(mnJsonSchema.micronaut.json.schema.processor)
    implementation(mnJsonSchema.micronaut.json.schema.utils)
    implementation(mn.micronaut.inject.python)
    implementation(mn.micronaut.context.python)
    implementation(projects.micronautLangchain4jCore)
    implementation(projects.micronautLangchain4jGoogleGenai)
    implementation(projects.micronautLangchain4jAgentic)
    implementation(libs.langchain4j.agentic.mcp)
    implementation(projects.micronautLangchain4jOpenai)
    implementation(projects.micronautLangchain4jOllama)
    // The Java helper of src/test/java (Ollama context configurer) is processed by javac
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.python.test)
    testImplementation(projects.testSuiteUtils)
    testImplementation(projects.micronautLangchain4jTest)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.api)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(platform(mnReactor.boms.reactor))
    testImplementation(libs.reactor.core)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mn.micronaut.http.client)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("micronaut.python.pool.enabled", "false")
}
