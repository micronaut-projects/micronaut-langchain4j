plugins {
    id ("io.micronaut.build.internal.java-base")
}
dependencies {
    implementation(projects.micronautLangchain4jCore)
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mnTest.micronaut.test.junit5)
    implementation(libs.awaitility)
    implementation(libs.langchain4j.reactor)
    implementation(platform(mnReactor.boms.reactor))
    implementation(libs.reactor.test)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
