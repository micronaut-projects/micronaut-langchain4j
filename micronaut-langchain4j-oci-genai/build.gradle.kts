plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(platform(libs.boms.langchain4j.community))
    api(platform(mnOraclecloud.micronaut.oraclecloud.bom))
    implementation(libs.langchain4j.oci.genai)
    api(libs.micronaut.oraclecloud.bmc.generativeaiinference)
    // TODO: remove when non-vulnerable version released
    // apply tools.jackson.core:jackson-core directly because versions prior to 3.1.1 are vulnerable to GHSA-2m67-wjpj-xhg9
    implementation(libs.tools.jackson.core)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
micronautBuild {
    binaryCompatibility.enabledAfter("1.0.1")
}
