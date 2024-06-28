pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.build.shared.settings") version "6.6.1"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "micronaut-langchain4j-parent"

include("micronaut-langchain4j-bom")
include("micronaut-langchain4j-processor")
include("micronaut-langchain4j-core")
include("micronaut-langchain4j-ollama")
include("micronaut-langchain4j-anthropic")
include("micronaut-langchain4j-azure")
include("micronaut-langchain4j-bedrock")
include("micronaut-langchain4j-hugging-face")
include("micronaut-langchain4j-openai")
include("micronaut-langchain4j-vertexai")
include("micronaut-langchain4j-mistralai")
include("micronaut-langchain4j-vertexai-gemini")
include("micronaut-langchain4j-ollama-testresource")
include("micronaut-langchain4j-qdrant-testresource")
include("micronaut-langchain4j-store-elasticsearch")
include("micronaut-langchain4j-store-opensearch")
include("micronaut-langchain4j-store-mongodb")
include("micronaut-langchain4j-store-neo4j")
include("micronaut-langchain4j-store-qdrant")
include("micronaut-langchain4j-store-redis")
include("micronaut-langchain4j-store-pgvector")
include("doc-examples:example-groovy")
include("doc-examples:example-java")
include("doc-examples:example-openai-java")
include("doc-examples:example-kotlin")

val micronautVersion = providers.gradleProperty("micronautVersion")

configure<io.micronaut.build.MicronautBuildSettingsExtension> {
    useStandardizedProjectNames.set(true)
    importMicronautCatalog()
    importMicronautCatalog("micronaut-sourcegen")
    importMicronautCatalog("micronaut-test-resources")
    importMicronautCatalog("micronaut-logging")
    importMicronautCatalog("micronaut-elasticsearch")
    importMicronautCatalog("micronaut-mongodb")
    importMicronautCatalog("micronaut-neo4j")
    importMicronautCatalog("micronaut-opensearch")
    importMicronautCatalog("micronaut-redis")
    importMicronautCatalog("micronaut-serde")
    importMicronautCatalog("micronaut-sql")
    // importMicronautCatalog("micronaut-validation")
}
