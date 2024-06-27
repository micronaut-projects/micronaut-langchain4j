plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSourcegen.micronaut.sourcegen.generator.java)
    api(projects.micronautLangchain4jCore)
    implementation(libs.langchain4j.hugging.face)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}