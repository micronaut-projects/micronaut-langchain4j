plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

description = """
Provides core support for QDrant test resources.
"""

dependencies {
    api(mnTestResources.micronaut.test.resources.core)
    api(mnTestResources.micronaut.test.resources.testcontainers)
    implementation("org.testcontainers:qdrant")
    implementation(libs.langchain4j.qdrant)
    implementation(mnGrpc.protobuf.java) // apply com.google.protobuf:protobuf-java directly because the version brought transitively contains a vulnerable version.
    implementation(libs.commons.compress) // declare the apache commons compress directly as the version from langchain4j has a security vulnerability
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}
