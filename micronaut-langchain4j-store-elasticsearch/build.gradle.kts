plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}
dependencies {
    api(mnElasticsearch.micronaut.elasticsearch)
    implementation(libs.langchain4j.elasticsearch)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
