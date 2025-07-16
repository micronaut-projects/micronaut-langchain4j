plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}
dependencies {
    api(libs.langchain4j.cassandra)
}

micronautBuild {
    binaryCompatibility {
        enabledAfter("1.2.0")
    }
}
