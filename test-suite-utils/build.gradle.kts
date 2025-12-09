plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    api(mn.jspecify)
    implementation(mnTest.micronaut.test.junit5)
    implementation(platform(mnTestResources.boms.testcontainers))
    implementation("org.testcontainers:ollama")
    implementation(libs.testcontainers.redis)
    implementation(libs.testcontainers.neo4j)
    implementation(libs.testcontainers.cassandra)
    implementation(libs.astra.db.client)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
