plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.open.ai)
}
tasks {
    javadoc {
        enabled = false
    }
}