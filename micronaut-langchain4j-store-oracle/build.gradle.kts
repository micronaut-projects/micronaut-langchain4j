plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.oracle)
    api(mnSql.micronaut.jdbc)
    constraints {
        // TODO: remove when langchain4j-oracle brings jsoup 1.23.1 or later (langchain4j 1.20.0)
        api(libs.jsoup) {
            because("GHSA-pmhh-3w7g-xqp8: langchain4j-oracle brings jsoup 1.18.3")
        }
    }
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testImplementation(mnSql.micronaut.jdbc.hikari)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.jdbc.oracle.free)
}
