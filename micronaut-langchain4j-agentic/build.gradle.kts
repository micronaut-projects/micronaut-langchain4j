plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(projects.micronautLangchain4jCore)
    api(libs.langchain4j)
    api(libs.langchain4j.agentic)
    testAnnotationProcessor(projects.micronautLangchain4jProcessor)
}

micronautBuild {
    binaryCompatibility.enabledAfter("2.2.0")
}
