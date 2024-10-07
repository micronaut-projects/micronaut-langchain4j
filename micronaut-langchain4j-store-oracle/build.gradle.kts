plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.oracle)
    api(mnSql.micronaut.jdbc)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testImplementation(mnSql.micronaut.jdbc.hikari)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.jdbc.oracle.free)
}
