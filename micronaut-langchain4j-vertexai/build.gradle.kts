plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.vertex.ai)
    // TODO: remove when non-vulnerable version released
    // apply com.google.protobuf:protobuf-java directly because the version brought transitively contains a vulnerable version.
    runtimeOnly(mnGrpc.protobuf.java)
}
