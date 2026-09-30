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
    implementation(mn.micronaut.inject.python)
    implementation(mn.micronaut.context.python)
    implementation(projects.micronautLangchain4jCore)
    implementation(projects.micronautLangchain4jAgentic)
    implementation(projects.micronautLangchain4jOpenai)
    implementation(projects.micronautLangchain4jOllama)
    // The Java helper of src/test/java (Ollama context configurer) is processed by javac
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.python.test)
    testImplementation(projects.testSuiteUtils)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.api)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mn.micronaut.http.client)
}

micronautBuild {
    python {
        // LangChain4j builds the AI and agentic services with java.lang.reflect.Proxy and reads their annotations
        // (@SystemMessage, @UserMessage, @Agent, @Tool, ...) reflectively from the generated interfaces and classes:
        // the compiler copies them for the classes named here (or declared @AllowsReflection)
        compilerArgs.add("-Amicronaut.introspection.allowReflection=example.micronaut.*")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("micronaut.python.pool.enabled", "false")
}
