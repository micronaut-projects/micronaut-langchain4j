plugins {
    id ("io.micronaut.build.internal.java-base")
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    api(mn.jspecify)
    implementation(mnTest.micronaut.test.junit5)
    implementation(platform(mnTest.boms.testcontainers))
    implementation(libs.testcontainers.ollama)
    implementation(libs.testcontainers.redis)
    implementation(libs.testcontainers.neo4j)
    implementation(libs.testcontainers.cassandra)
    implementation(libs.astra.db.client)
}
