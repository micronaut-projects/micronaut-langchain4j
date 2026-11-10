plugins {
    id("io.micronaut.build.internal.langchain4j-module-provider")
}

dependencies {
    api(platform(libs.boms.langchain4j.community))
    implementation(libs.langchain4j.redis)
    implementation(libs.gson) // versions prior to 2.12.0 contains a CVE https://ossindex.sonatype.org/component/pkg:maven/com.google.code.gson/gson
    implementation(libs.org.json) // GHSA-ghc9-756g-vrcv: Jedis brings org.json 20250517
}

val generatePom = tasks.named<GenerateMavenPom>("generatePomFileForMavenPublication")
val verifyPatchedOrgJson by tasks.registering {
    description = "Verifies that Maven consumers get the patched org.json"
    val pom = generatePom.map { it.destination }
    val expected = libs.versions.org.json
    inputs.file(pom)
    doLast {
        val dependency = Regex("<groupId>org\\.json</groupId>\\s*<artifactId>json</artifactId>\\s*<version>([^<]+)</version>")
        val version = dependency.find(pom.get().readText())?.groupValues?.get(1)
        check(version == expected.get()) { "The published POM declares org.json $version instead of ${expected.get()}" }
    }
}
tasks.named("check") { dependsOn(verifyPatchedOrgJson) }
