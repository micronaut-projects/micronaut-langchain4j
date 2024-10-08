plugins {
    id("io.micronaut.build.internal.langchain4j-java-library")
}
dependencies {
    implementation(projects.micronautLangchain4jCore)
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mnTest.micronaut.test.junit5)
    implementation(libs.awaitility)
}
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

