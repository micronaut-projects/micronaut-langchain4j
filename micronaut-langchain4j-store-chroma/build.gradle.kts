plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.chroma)
}

micronautBuild {
    binaryCompatibility.enabledAfter("2.2.0")
}
