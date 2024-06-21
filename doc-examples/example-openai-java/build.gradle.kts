plugins {
    id("java")
    id("io.micronaut.build.internal.langchain4j-example")

}

micronaut {
    runtime("netty")
    testRuntime("junit5")
}
dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    implementation(projects.micronautLangchain4jOpenai)
    implementation(mn.micronaut.http.client)
    implementation(mnSerde.micronaut.serde.jackson)
    runtimeOnly(mnLogging.logback.classic)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
