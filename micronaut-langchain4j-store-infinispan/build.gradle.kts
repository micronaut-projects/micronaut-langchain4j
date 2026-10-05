plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.infinispan)
    testImplementation(libs.langchain4j.embeddings.all.minilm.l6.v2)
    testRuntimeOnly(projects.micronautLangchain4jInfinispanTestresource)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)

}

micronautBuild {
    // This new module has no released artifact to use as a compatibility baseline.
    binaryCompatibility.enabled = false
}
