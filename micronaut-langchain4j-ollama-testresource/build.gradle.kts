plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

description = """
Provides core support for OLLama test resources.
"""

dependencies {
    api(mnTestResources.micronaut.test.resources.core)
    api(mnTestResources.micronaut.test.resources.testcontainers)
    implementation("org.testcontainers:ollama")
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    implementation(libs.commons.compress) // declare the apache commons compress directly as the version from langchain4j has a security vulnerability
}

