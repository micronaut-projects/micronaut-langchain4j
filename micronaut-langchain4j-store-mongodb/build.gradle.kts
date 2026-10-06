plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}
dependencies {
    api(mnMongo.micronaut.mongo.sync)
    implementation(libs.langchain4j.mongodb.atlas)
    implementation(platform(libs.boms.langchain4j.community))
    implementation(libs.langchain4j.community.mongodb)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
