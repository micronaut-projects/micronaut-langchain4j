plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(platform(libs.langchain4j.community.bom))
    implementation(libs.langchain4j.redis)
    implementation(libs.gson) // versions prior to 2.12.0 contains a CVE https://ossindex.sonatype.org/component/pkg:maven/com.google.code.gson/gson
}
