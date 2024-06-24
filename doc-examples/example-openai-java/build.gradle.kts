plugins {
    id("java")
    id("io.micronaut.build.internal.langchain4j-example")

}

micronaut {
    runtime("netty")
    testRuntime("junit5")
}
dependencies {
    implementation(platform(libs.boms.langchain4j))
    annotationProcessor(projects.micronautLangchain4jProcessor)
    implementation(projects.micronautLangchain4jOpenai)
    implementation(mn.micronaut.http.client)
    implementation(mnSerde.micronaut.serde.jackson)
    runtimeOnly(mnLogging.logback.classic)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox")
    testImplementation("dev.langchain4j:langchain4j-embeddings-e5-small-v2-q")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    test {
        jvmArgs("-Xmx1024m")
    }
}
