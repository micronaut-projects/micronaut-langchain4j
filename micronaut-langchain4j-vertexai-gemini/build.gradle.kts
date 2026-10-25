plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.vertex.ai.gemini)
    // TODO: remove when non-vulnerable version released
    // apply com.google.protobuf:protobuf-java directly because the version brought transitively contains a vulnerable version.
    runtimeOnly(mnGrpc.protobuf.java)
    // TODO: remove when non-vulnerable version released
    // apply io.grpc:grpc-netty-shaded directly because versions prior to 1.75.0 are vulnerable to GHSA-prj3-ccx8-p6x4
    runtimeOnly(libs.grpc.netty.shaded)
}
