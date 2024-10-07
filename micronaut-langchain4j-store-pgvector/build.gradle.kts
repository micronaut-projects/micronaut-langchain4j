plugins {
    id("io.micronaut.build.internal.langchain4j-module")
//    id("io.micronaut.test-resources")
}

dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSourcegen.micronaut.sourcegen.generator.java)
    api(projects.micronautLangchain4jCore)
    api(libs.langchain4j.pgvector)
    api(mnSql.micronaut.jdbc)
    runtimeOnly(mnSql.postgresql)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testImplementation(mnSql.micronaut.jdbc.hikari)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.jdbc.postgresql)
}
