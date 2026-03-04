plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}
dependencies {
    implementation(platform(libs.boms.langchain4j.community))
    api(mnNeo4j.micronaut.neo4j.bolt)
    implementation(libs.langchain4j.neo4j)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
