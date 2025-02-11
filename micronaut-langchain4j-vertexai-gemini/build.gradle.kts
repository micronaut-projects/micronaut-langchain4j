plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(libs.langchain4j.vertex.ai.gemini)
    // apply com.google.protobuf:protobuf-java directly because the version brought transitively contains a vulnerable version.
    implementation(mnGrpc.protobuf.java)

    // TODO: remove when non-vulnerable version released
    runtimeOnly(libs.threetenbp)
}
