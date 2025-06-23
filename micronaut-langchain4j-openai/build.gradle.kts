plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(platform(libs.langchain4j.bom))
    implementation(libs.langchain4j.open.ai)
}
