plugins {
    `java-library`
    id ("io.micronaut.build.internal.kotlin-base")
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(mnTest.micronaut.test.junit5)
    implementation(platform(mnTestResources.boms.testcontainers))
    implementation("org.testcontainers:ollama")
    implementation(libs.testcontainers.redis)
    implementation(libs.testcontainers.neo4j)
    implementation(libs.testcontainers.cassandra)
    implementation(libs.astra.db.client)
}
