plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

description = """
Provides core support for Infinispan test resources.
"""

dependencies {
    api(mnTestResources.micronaut.test.resources.core)
    api(mnTestResources.micronaut.test.resources.testcontainers)
    implementation(platform(mnTest.boms.testcontainers))
    implementation(libs.testcontainers)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}

micronautBuild {
    // This new module has no released artifact to use as a compatibility baseline.
    binaryCompatibility.enabled = false
}
