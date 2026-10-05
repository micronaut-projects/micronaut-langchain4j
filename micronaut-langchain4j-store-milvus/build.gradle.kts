plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.milvus.v2)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.milvus)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
