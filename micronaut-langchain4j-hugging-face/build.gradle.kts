plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    testImplementation(projects.testSuiteUtils)
    implementation(libs.langchain4j.hugging.face)
}
