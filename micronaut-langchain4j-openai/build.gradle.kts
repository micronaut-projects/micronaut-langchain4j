plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    implementation(platform(libs.boms.langchain4j))
    implementation(libs.langchain4j.open.ai) {
        exclude(group = "dev.langchain4j", module = "langchain4j-http-client-jdk")
    }
    testImplementation(mn.micronaut.http.client.core)
    testImplementation(platform(mnReactor.boms.reactor))
    testImplementation(libs.reactor.core)
    testImplementation(mnSecurity.micronaut.security)
    testImplementation(mn.micronaut.http.server.netty)
    testRuntimeOnly(mn.micronaut.http.client)
    testRuntimeOnly(mn.micronaut.jackson.databind)
}
