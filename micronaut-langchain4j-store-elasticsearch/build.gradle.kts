plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}
dependencies {
    api(mnElasticsearch.micronaut.elasticsearch)
    implementation(libs.langchain4j.elasticsearch)
    // TODO: remove when non-vulnerable version released
    // apply tools.jackson.core:jackson-core directly because versions prior to 3.1.1 are vulnerable to GHSA-2m67-wjpj-xhg9
    implementation(libs.tools.jackson.core)
    constraints {
        // TODO: remove when micronaut-elasticsearch or elasticsearch-rest5-client bring non-vulnerable versions
        implementation(libs.apache.httpclient5) {
            because("GHSA-hjcp-jmpx-g3qm: elasticsearch-rest5-client brings httpclient5 5.6.1")
        }
        implementation(libs.apache.httpcore5) {
            because("GHSA-hf6x-8p5f-cgmf: elasticsearch-rest5-client brings httpcore5 5.4")
        }
        implementation(libs.apache.httpcore5.h2) {
            because("GHSA-v3jc-474w-2wm6: elasticsearch-rest5-client brings httpcore5-h2 5.4")
        }
    }
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
}
