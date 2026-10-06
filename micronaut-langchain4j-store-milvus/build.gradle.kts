plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.milvus.v2)
    constraints {
        // TODO: remove when milvus-sdk-java brings non-vulnerable versions
        implementation(libs.grpc.netty.shaded) {
            because("GHSA-prj3-ccx8-p6x4: milvus-sdk-java brings grpc-netty-shaded 1.59.1")
        }
        implementation(libs.commons.lang3) {
            because("GHSA-j288-q9x7-2f5v: milvus-sdk-java brings commons-lang3 3.12.0")
        }
    }
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.milvus)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
