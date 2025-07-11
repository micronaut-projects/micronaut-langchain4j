plugins {
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(platform(mnTestResources.boms.testcontainers))
    implementation("org.testcontainers:ollama")
}
