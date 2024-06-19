plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    implementation(platform(libs.boms.langchain4j))
    implementation(mn.micronaut.core.processor)
    implementation(mnSourcegen.micronaut.sourcegen.generator)
    implementation(projects.micronautLangchain4jCore)
    testImplementation(mn.micronaut.inject.java.test)
    testImplementation(libs.langchain4j.ollama)
    testImplementation(mnSourcegen.micronaut.sourcegen.generator.java)
}
