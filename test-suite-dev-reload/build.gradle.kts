plugins {
    id("io.micronaut.build.internal.java-base")
    jacoco
}

configurations.testRuntimeClasspath {
    // the reload harness runs every annotation processor of the test classpath on the application it compiles, and
    // Lombok, which a test container client brings, does not run on this JDK
    exclude(group = "org.projectlombok", module = "lombok")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(platform(libs.boms.langchain4j))
    testImplementation(projects.micronautLangchain4jCore)
    testImplementation(projects.micronautLangchain4jAgentic)
    testImplementation(projects.micronautLangchain4jOllama)
    // the reload harness compiles the application under test with the processors on the test classpath, the
    // LangChain4j one included, which makes @Tool methods executable
    testImplementation(projects.micronautLangchain4jProcessor)
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.dev.tck)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mnSerde.micronaut.serde.jackson)
    // a LangChain4j HTTP client that is not bound to the context, which a model built on it can keep across a restart
    testImplementation(libs.langchain4j.http.client.jdk)
    testImplementation(projects.testSuiteUtils)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.ollama)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
}

// The development support of these modules runs in the reload tests here, so their coverage is reported for them
val reloadedProjects = listOf(projects.micronautLangchain4jCore, projects.micronautLangchain4jAgentic).map { project(it.path) }

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    sourceDirectories.setFrom(reloadedProjects.map { it.file("src/main/java") })
    classDirectories.setFrom(reloadedProjects.map { it.layout.buildDirectory.dir("classes/java/main") })
    reports.xml.required = true
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
}
