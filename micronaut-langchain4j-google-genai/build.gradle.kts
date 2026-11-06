plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.google.genai)
    testImplementation(projects.testSuiteUtils)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
