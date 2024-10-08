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
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}

micronautBuild {
    // new module, so disable binary check for now
    binaryCompatibility {
        enabled.set(false)
    }
}
