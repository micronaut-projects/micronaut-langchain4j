plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.mistral.ai)
    testImplementation(projects.testSuiteChatmodelTck)
    testImplementation(libs.junit.platform.engine)
}
