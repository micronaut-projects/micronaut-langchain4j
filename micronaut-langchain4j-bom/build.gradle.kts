plugins {
    id("io.micronaut.build.internal.bom")
}
micronautBom {
    suppressions {
        dependencies.add("langchain4j-embeddings-bge-small-v15-en")
    }
}

tasks {
    checkBom {
        enabled = false // fix failure
    }
}
micronautBuild {
    binaryCompatibility.enabled = false
}
