plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.hugging.face)
}


tasks {
    javadoc {
        enabled = false
    }
}