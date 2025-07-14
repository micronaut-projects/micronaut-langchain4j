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
    testImplementation(platform(mnTestResources.boms.testcontainers))
    testImplementation(platform(libs.langchain4j.community.bom))
    testImplementation(libs.langchain4j.community.redis)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.langchain4j.community.neo4j)
    testImplementation(libs.langchain4j.cassandra)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)


}
tasks.withType<Test> {
    useJUnitPlatform()
}
