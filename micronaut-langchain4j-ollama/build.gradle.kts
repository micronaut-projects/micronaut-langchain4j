plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.ollama) {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(libs.langchain4j.ollama) {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(projects.micronautLangchain4jOllamaTestresource)
}
