plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    // Agentic API lives in langchain4j core
    implementation(platform(libs.langchain4j.bom))
    api(libs.langchain4j)
    api(libs.langchain4j.agentic)
    implementation(libs.micronaut.inject.java)
}
