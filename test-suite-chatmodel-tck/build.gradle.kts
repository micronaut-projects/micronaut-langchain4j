plugins {
    id("io.micronaut.build.internal.langchain4j-java-library")
    id ("io.micronaut.build.internal.java-base")
}
dependencies {
    implementation(projects.micronautLangchain4jCore)
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mnTest.micronaut.test.junit5)
    implementation(libs.awaitility)
}
