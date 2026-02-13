plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    ksp("io.micronaut:micronaut-inject-kotlin")
    kspTest("io.micronaut:micronaut-inject-kotlin")
    implementation(mnKotlin.micronaut.kotlin.runtime)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.25")

    ksp(project(":micronaut-langchain4j-processor"))
    implementation(project(":micronaut-langchain4j-core"))
    implementation(project(":micronaut-langchain4j-openai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))
    testImplementation(project(":test-suite-utils"))
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.withType<Test> {
    useJUnitPlatform()
}
