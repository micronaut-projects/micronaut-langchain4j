plugins {
    id("io.micronaut.build.internal.langchain4j-module")
    id("io.micronaut.test-resources")
}

dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSourcegen.micronaut.sourcegen.generator.java)
    api(projects.micronautLangchain4jCore)
    api(mnNeo4j.micronaut.neo4j.bolt)
    implementation(libs.langchain4j.neo4j)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
}
