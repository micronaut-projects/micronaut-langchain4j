plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSourcegen.micronaut.sourcegen.generator.java)
    api(projects.micronautLangchain4jCore)
    api(libs.langchain4j.jlama)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}

micronautBuild {
    javaVersion = 21
}

tasks {
    test {
        jvmArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")
    }
}
