plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.google.ai.gemini) {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
}
