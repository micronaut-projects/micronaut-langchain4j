plugins {
    `java-library`
    id("org.graalvm.buildtools.native")
}
repositories {
    mavenCentral()
}
dependencies {
    testAnnotationProcessor(mn.micronaut.inject.java)
    testAnnotationProcessor(mn.micronaut.graal)
    testAnnotationProcessor(projects.micronautLangchain4jProcessor)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(projects.micronautLangchain4jCore)
    testImplementation(projects.micronautLangchain4jAgentic)
    testImplementation(projects.micronautLangchain4jSerde)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// The AI services, agents and tools of the tests run in a native image built without hand-written reflection or proxy
// configuration: the metadata comes from micronaut-langchain4j-processor. Run with ./gradlew :test-suite-graal:nativeTest
graalvmNative {
    toolchainDetection.set(false)
}
