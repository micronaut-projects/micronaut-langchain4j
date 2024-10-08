plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
    id("io.micronaut.test-resources")
}

dependencies {
    api(mnOpensearch.micronaut.opensearch)
    implementation(libs.langchain4j.opensearch)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
tasks {
    javadoc {
        enabled = false
    }
}