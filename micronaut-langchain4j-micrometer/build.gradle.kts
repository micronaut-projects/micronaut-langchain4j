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

tasks.test {
    filter.excludeTestsMatching("*WithoutObservationTest")
}

// the metrics listener must be registered when langchain4j-observation is absent
val testWithoutObservation by tasks.registering(Test::class) {
    description = "Runs the tests that require langchain4j-observation to be absent from the classpath."
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath.filter { !it.name.startsWith("langchain4j-observation-") }
    filter.includeTestsMatching("*WithoutObservationTest")
    useJUnitPlatform()
}

tasks.check {
    dependsOn(testWithoutObservation)
}
