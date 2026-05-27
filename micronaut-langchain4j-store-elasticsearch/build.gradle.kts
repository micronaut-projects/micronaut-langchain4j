plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}
dependencies {
    api(mnElasticsearch.micronaut.elasticsearch)
    implementation(libs.langchain4j.elasticsearch)
    // TODO: remove when non-vulnerable version released
    // apply tools.jackson.core:jackson-core directly because versions prior to 3.1.1 are vulnerable to GHSA-2m67-wjpj-xhg9
    implementation(libs.tools.jackson.core)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
