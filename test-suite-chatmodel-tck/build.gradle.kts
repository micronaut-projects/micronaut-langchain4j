plugins {
    id("io.micronaut.build.internal.langchain4j-java-library")
}
dependencies {
    implementation(projects.micronautLangchain4jCore)
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mnTest.micronaut.test.junit5)
}
