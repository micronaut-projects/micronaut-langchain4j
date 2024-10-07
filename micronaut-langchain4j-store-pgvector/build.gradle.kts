plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
//    id("io.micronaut.test-resources")
}

dependencies {
    api(libs.langchain4j.pgvector)
    api(mnSql.micronaut.jdbc)
    runtimeOnly(mnSql.postgresql)

    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testImplementation(mnSql.micronaut.jdbc.hikari)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.jdbc.postgresql)
}
