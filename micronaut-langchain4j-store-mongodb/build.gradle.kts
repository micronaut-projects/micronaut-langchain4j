plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
    id("io.micronaut.test-resources")
}

dependencies {
    api(mnMongo.micronaut.mongo.sync)
    implementation(libs.langchain4j.mongodb.atlas)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
