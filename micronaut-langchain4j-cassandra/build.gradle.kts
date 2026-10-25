plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}
dependencies {
    api(libs.langchain4j.cassandra)
    constraints {
        // TODO: remove when astra-db-client brings non-vulnerable versions (1.2.7 still pins httpclient5 5.3)
        api(libs.apache.httpclient5) {
            because("GHSA-hjcp-jmpx-g3qm: astra-db-client brings httpclient5 5.3")
        }
        api(libs.apache.httpcore5) {
            because("GHSA-hf6x-8p5f-cgmf: astra-db-client brings httpcore5 5.2.4")
        }
        api(libs.apache.httpcore5.h2) {
            because("GHSA-v3jc-474w-2wm6: astra-db-client brings httpcore5-h2 5.2.4")
        }
    }
}

micronautBuild {
    binaryCompatibility {
        enabledAfter("1.2.0")
    }
}
