plugins {
    id("io.micronaut.build.internal.langchain4j-module")
    id("io.micronaut.test-resources")
}

dependencies {
    annotationProcessor(projects.micronautLangchain4jProcessor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSourcegen.micronaut.sourcegen.generator.java)
    api(projects.micronautLangchain4jCore)
    api(mnElasticsearch.micronaut.elasticsearch)
    implementation(libs.langchain4j.elasticsearch)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTest.micronaut.test.junit5)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
