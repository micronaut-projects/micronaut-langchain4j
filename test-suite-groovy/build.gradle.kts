plugins {
    `java-library`
    groovy
}
repositories {
    mavenCentral()
}
dependencies {
    compileOnly(mn.micronaut.inject.groovy)
    implementation(mnGroovy.micronaut.runtime.groovy)
    compileOnly(project(":micronaut-langchain4j-processor"))
    implementation(project(":micronaut-langchain4j-core"))
    implementation(project(":micronaut-langchain4j-openai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))

    testImplementation(project(":test-suite-utils"))
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}
tasks.withType<Test> {
    useJUnitPlatform()
}
