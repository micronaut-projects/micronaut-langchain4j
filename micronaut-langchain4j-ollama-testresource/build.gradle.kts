plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

description = """
Provides core support for OLLama test resources.
"""

dependencies {
    api(mnTestResources.micronaut.test.resources.core)
    api(mnTestResources.micronaut.test.resources.testcontainers)
    implementation(platform(mnTest.boms.testcontainers))
    implementation(libs.testcontainers.ollama)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTest.junit.jupiter.engine)

    implementation(libs.commons.compress) // declare the apache commons compress directly as the version from langchain4j has a security vulnerability
    implementation(libs.commons.lang3) // versions prior to 3.18.0 contains a CVE https://ossindex.sonatype.org/component/pkg:maven/org.apache.commons/commons-lang3
}

