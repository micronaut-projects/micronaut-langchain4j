plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    implementation(mn.micronaut.core.processor)
    implementation(mnSourcegen.micronaut.sourcegen.generator)
    implementation(mnSourcegen.micronaut.sourcegen.generator.java)
    implementation(projects.micronautLangchain4jCore)
    testImplementation(mn.micronaut.inject.java.test)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnSourcegen.micronaut.sourcegen.generator.java)
}
