plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(platform(libs.boms.langchain4j.community))
    implementation(libs.langchain4j.redis)
    implementation(libs.gson) // versions prior to 2.12.0 contains a CVE https://ossindex.sonatype.org/component/pkg:maven/com.google.code.gson/gson
    constraints {
        // TODO: remove when Jedis brings a non-vulnerable version
        implementation(libs.org.json) {
            because("GHSA-ghc9-756g-vrcv: Jedis brings org.json 20250517")
        }
    }
}
