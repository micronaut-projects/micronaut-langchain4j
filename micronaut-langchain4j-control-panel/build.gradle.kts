plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(projects.micronautLangchain4jCore)
    api(mnControlPanel.micronaut.control.panel.core)
    implementation(mnSerde.micronaut.serde.api)
    annotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(mnControlPanel.micronaut.control.panel.ui)
    testImplementation(mn.micronaut.management)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testAnnotationProcessor(projects.micronautLangchain4jProcessor)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
