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
include("micronaut-langchain4j-ollama-testresource")
include("doc-examples:example-groovy")
include("doc-examples:example-java")
include("doc-examples:example-kotlin")

val micronautVersion = providers.gradleProperty("micronautVersion")

configure<io.micronaut.build.MicronautBuildSettingsExtension> {
    useStandardizedProjectNames.set(true)
    importMicronautCatalog()
    importMicronautCatalog("micronaut-test-resources")
    importMicronautCatalog("micronaut-logging")
    // importMicronautCatalog("micronaut-validation")
}
