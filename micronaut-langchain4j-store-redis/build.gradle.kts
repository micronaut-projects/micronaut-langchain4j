import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

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
        // only the project's own dependencies reach consumers, not its dependencyManagement
        fun Element.children(name: String) = (0 until childNodes.length).map { childNodes.item(it) }
            .filterIsInstance<Element>().filter { it.tagName == name }
        fun Element.text(name: String) = children(name).firstOrNull()?.textContent?.trim()
        val project = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.get()).documentElement
        val dependency = project.children("dependencies").flatMap { it.children("dependency") }
            .firstOrNull { it.text("groupId") == "org.json" && it.text("artifactId") == "json" }
        checkNotNull(dependency) { "The published POM has no dependency on org.json, so Maven consumers get the version Jedis brings" }
        val version = dependency.text("version")
        val scope = dependency.text("scope") ?: "compile"
        check(version == expected.get() && scope in setOf("compile", "runtime")) {
            "The published POM declares org.json $version ($scope) instead of ${expected.get()} (compile or runtime)"
        }
    }
}
tasks.named("check") { dependsOn(verifyPatchedOrgJson) }
