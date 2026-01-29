plugins {
    id("java")
    id("io.micronaut.build.internal.langchain4j-example")
    id("io.micronaut.test-resources")
}

micronaut {
    version.set(libs.versions.micronaut.platform.get())
    runtime("netty")
    testRuntime("junit5")
}
dependencies {
    implementation(platform(libs.langchain4j.bom))

    annotationProcessor(projects.micronautLangchain4jProcessor)
    implementation(projects.micronautLangchain4jOpenai)
    implementation(projects.micronautLangchain4jStoreQdrant)
    implementation(mn.micronaut.http.client)
    implementation(mnSerde.micronaut.serde.jackson)
    implementation("dev.langchain4j:langchain4j-reactor")
    runtimeOnly(mnLogging.logback.classic)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox")
    testImplementation("dev.langchain4j:langchain4j-embeddings-e5-small-v2-q")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testResourcesService(projects.micronautLangchain4jQdrantTestresource)
}

tasks {
    test {
        jvmArgs("-Xmx1024m")
    }
}
