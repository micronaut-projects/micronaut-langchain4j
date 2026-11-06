plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    testImplementation(projects.testSuiteUtils)
    implementation(libs.langchain4j.jina) {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
    testRuntimeOnly(mn.micronaut.http.client)
    testRuntimeOnly(mn.micronaut.jackson.databind)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
