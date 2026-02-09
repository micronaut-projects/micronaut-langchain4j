plugins {
    id("io.micronaut.build.internal.langchain4j-java-library")
}
dependencies {
    implementation(projects.micronautLangchain4jCore)
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mnTest.micronaut.test.junit5)
    implementation(libs.awaitility)
    implementation(libs.langchain4j.reactor)
    implementation(platform(mnReactor.boms.reactor))
    implementation(libs.reactor.test)
}
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

