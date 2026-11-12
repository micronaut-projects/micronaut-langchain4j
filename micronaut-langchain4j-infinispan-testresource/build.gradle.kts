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
    implementation(libs.testcontainers.infinispan)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}

micronautBuild {
    // This new module has no released artifact to use as a compatibility baseline.
    binaryCompatibility.enabled = false
}
