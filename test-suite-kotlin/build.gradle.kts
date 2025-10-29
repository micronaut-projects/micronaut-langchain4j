plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.ksp)
    `java-library`
    id ("io.micronaut.build.internal.kotlin-base")
}
repositories {
    mavenCentral()
}
dependencies {
    ksp("io.micronaut:micronaut-inject-kotlin")
    kspTest("io.micronaut:micronaut-inject-kotlin")
    implementation(mnKotlin.micronaut.kotlin.runtime)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)
    ksp(project(":micronaut-langchain4j-processor"))
    implementation(project(":micronaut-langchain4j-core"))
    implementation(project(":micronaut-langchain4j-openai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))
    testImplementation(project(":test-suite-utils"))
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(platform(mnTestResources.boms.testcontainers))
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)
    // Ensure JUnit Platform engine is on the test runtime classpath
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.withType<Test> {
    useJUnitPlatform()
}
