plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.jlama)
    testImplementation(projects.testSuiteChatmodelTck)
    testImplementation(libs.junit.platform.engine)
}

micronautBuild {
    javaVersion = 21
}

tasks {
    test {
        jvmArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")
    }
}
