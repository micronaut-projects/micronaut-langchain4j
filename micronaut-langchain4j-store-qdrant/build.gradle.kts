plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.qdrant)
    implementation(mnGrpc.protobuf.java) // apply com.google.protobuf:protobuf-java directly because the version brought transitively contains a vulnerable version.
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(projects.micronautLangchain4jQdrantTestresource)
}
