plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(projects.micronautLangchain4jCore)
    api(mnSerde.micronaut.serde.api)
    implementation(mnSerde.micronaut.serde.jackson)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testAnnotationProcessor(projects.micronautLangchain4jProcessor)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
