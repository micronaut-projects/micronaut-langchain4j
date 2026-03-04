plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(projects.micronautLangchain4jProcessor)
    implementation(projects.micronautLangchain4jCore)
    implementation(projects.micronautLangchain4jOpenai)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(projects.micronautLangchain4jOllama)

    testImplementation(projects.testSuiteUtils)
    testImplementation(projects.micronautLangchain4jStoreRedis)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(projects.micronautLangchain4jStoreNeo4j)
    testImplementation(projects.micronautLangchain4jCassandra)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnTest.junit.jupiter.engine)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(mnLogging.logback.classic)
    // Ensure JUnit Platform engine is on the test runtime classpath
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly(mnTest.junit.platform.launcher)
}
tasks.withType<Test> {
    useJUnitPlatform()
}
