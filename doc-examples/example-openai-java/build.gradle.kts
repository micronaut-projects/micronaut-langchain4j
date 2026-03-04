plugins {
    id ("io.micronaut.build.internal.java-base")
    id("io.micronaut.build.internal.langchain4j-example")
}

dependencies {
    implementation(platform(libs.boms.langchain4j))
    annotationProcessor(projects.micronautLangchain4jProcessor)
    testAnnotationProcessor(mn.micronaut.inject.java)
    implementation(projects.micronautLangchain4jOpenai)
    implementation(projects.micronautLangchain4jStoreQdrant)
    implementation(mn.micronaut.http.client)
    implementation(mnSerde.micronaut.serde.jackson)
    runtimeOnly(mnLogging.logback.classic)
    testImplementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox")
    testImplementation("dev.langchain4j:langchain4j-embeddings-e5-small-v2-q")
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testImplementation(projects.micronautLangchain4jQdrantTestresource)
}
tasks {
    test {
        jvmArgs("-Xmx1024m")
    }
}
