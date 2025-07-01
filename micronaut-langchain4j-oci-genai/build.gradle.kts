plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(platform(libs.langchain4j.community.bom))
    api(platform(mnOraclecloud.micronaut.oraclecloud.bom))
    implementation(libs.langchain4j.oci.genai)
    api(libs.micronaut.oraclecloud.bmc.generativeaiinference)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
