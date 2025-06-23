plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(platform(libs.langchain4j.community.bom))
    implementation(libs.langchain4j.redis)
    implementation(libs.org.json) //force a version without CVE
}
