plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.google.ai.gemini)
}

tasks {
    javadoc {
        enabled = false
    }
}
