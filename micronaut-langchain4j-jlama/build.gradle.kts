plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.jlama)
}

micronautBuild {
    javaVersion = 21
}

tasks {
    javadoc {
        enabled = false
    }
    test {
        jvmArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")
    }
}
