plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.weaviate)
    constraints {
        // TODO: remove when the Weaviate client brings non-vulnerable versions
        implementation(libs.grpc.netty.shaded) {
            because("GHSA-prj3-ccx8-p6x4: the Weaviate client brings grpc-netty-shaded 1.68.2")
        }
        implementation(libs.commons.lang3) {
            because("GHSA-j288-q9x7-2f5v: the Weaviate client brings commons-lang3 3.17.0")
        }
        implementation(libs.apache.httpclient5) {
            because("GHSA-hjcp-jmpx-g3qm: the Weaviate client brings httpclient5 5.4.3")
        }
        implementation(libs.apache.httpcore5) {
            because("GHSA-hf6x-8p5f-cgmf: the Weaviate client brings httpcore5 5.3.4")
        }
        implementation(libs.apache.httpcore5.h2) {
            because("GHSA-v3jc-474w-2wm6: the Weaviate client brings httpcore5-h2 5.3.4")
        }
    }
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.weaviate)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
