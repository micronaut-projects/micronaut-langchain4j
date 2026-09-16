plugins {
    `java-library`
    id("io.micronaut.build.internal.python")
}
repositories {
    mavenCentral()
}
dependencies {
    // The Java helpers of src/test/java (Ollama context configurer, mock chat model) are processed by javac
    testAnnotationProcessor(mn.micronaut.inject.java)
    // Annotation processors of the Python sources MUST be testImplementation (not testAnnotationProcessor):
    // the Python compiler (micronaut-inject-python) takes the (jar-resolved) compile classpath as its annotation processor path.
    testImplementation(projects.micronautLangchain4jProcessor)
    testImplementation(mn.micronaut.inject.python.test)
    testImplementation(mn.micronaut.context.python)
    testImplementation(projects.micronautLangchain4jCore)
    testImplementation(projects.micronautLangchain4jAgentic)
    testImplementation(projects.micronautLangchain4jOpenai)
    testImplementation(projects.micronautLangchain4jOllama)
    testImplementation(projects.testSuiteUtils)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mn.micronaut.http.client)
}

// TODO(python): compiling src/main/python and src/test/python separately yields two GraalPy VFS roots whose
// generated shim modules shadow each other at test time, so the main Python sources (the `source="main"`
// documentation snippets) are compiled together with the test sources.
tasks.named("compilePython") { enabled = false }
tasks.named<io.micronaut.build.python.PythonCompile>("compileTestPython") {
    source.from(layout.projectDirectory.dir("src/main/python"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("micronaut.python.pool.enabled", "false")
}
