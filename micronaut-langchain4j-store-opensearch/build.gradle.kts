plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
    id("io.micronaut.test-resources")
}
micronaut {
    version.set(libs.versions.micronaut.platform.get())
}
dependencies {
    api(mnOpensearch.micronaut.opensearch)
    implementation(libs.langchain4j.opensearch)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
