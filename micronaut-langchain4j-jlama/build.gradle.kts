plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(libs.langchain4j.jlama)
    implementation(libs.commons.lang3) // versions prior to 3.18.0 contains a CVE https://ossindex.sonatype.org/component/pkg:maven/org.apache.commons/commons-lang3
}

micronautBuild {
    javaVersion = 21
}

tasks {
    test {
        jvmArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")
    }
}
micronautBuild {
    binaryCompatibility.enabled = false
}
