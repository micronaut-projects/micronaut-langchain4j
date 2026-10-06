plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(projects.micronautLangchain4jCore)
    api(libs.langchain4j.micrometer.metrics)
    implementation(mnMicrometer.micronaut.micrometer.core)
    compileOnly(libs.langchain4j.observation)
    compileOnly(mnMicrometer.micronaut.micrometer.observation)
    testImplementation(libs.langchain4j.observation)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(projects.micronautLangchain4jOpenai)
    testRuntimeOnly(mn.micronaut.http.client)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
