plugins {
    id("io.micronaut.build.internal.langchain4j-module")
}

dependencies {
    api(projects.micronautLangchain4jCore)
    api(platform(libs.boms.langchain4j.community))
    api(libs.langchain4j.community.sql)
    api(mnSql.micronaut.jdbc)
    testImplementation(mnSql.micronaut.jdbc.hikari)
    testRuntimeOnly(mnSql.h2)
    testRuntimeOnly(mnSql.postgresql)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.embedded)
    testRuntimeOnly(mnTestResources.micronaut.test.resources.jdbc.postgresql)
}

micronautBuild {
    binaryCompatibility.enabled = false
}
