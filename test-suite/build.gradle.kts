plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(project(":micronaut-langchain4j-processor"))
    implementation(project(":micronaut-langchain4j-core"))
    implementation(project(":micronaut-langchain4j-openai"))
    testImplementation(project(":micronaut-langchain4j-ollama"))

    testImplementation(project(":test-suite-utils"))
    testImplementation(project(":micronaut-langchain4j-store-redis"))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(project(":micronaut-langchain4j-store-neo4j"))
    testImplementation(libs.langchain4j.cassandra)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)


}
tasks.withType<Test> {
    useJUnitPlatform()
}
