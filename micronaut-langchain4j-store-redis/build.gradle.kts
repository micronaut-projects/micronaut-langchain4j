plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.redis)
    implementation(libs.org.json) //force a version without CVE
}
