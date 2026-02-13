plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(mnTest.micronaut.test.junit5)
    implementation(platform(libs.testcontainers.bom))
    implementation(libs.testcontainers.ollama)
    implementation(libs.testcontainers.redis)
    implementation(libs.testcontainers.neo4j)
    implementation(libs.testcontainers.cassandra)
    implementation(libs.astra.db.client)
}
