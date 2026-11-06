import org.sonarqube.gradle.SonarExtension

plugins {
    id("io.micronaut.build.internal.parent")
}

repositories {
    mavenCentral()
}

micronautBuild {
    javaVersion = 25
}

if (System.getenv("SONAR_TOKEN") != null) {
    configure<SonarExtension> {
        properties {
            property("sonar.exclusions", "**/example/**")
            // test-suite-utils is test infrastructure in a main source set, only exercised by other modules' tests
            property("sonar.coverage.exclusions", "**/testutils/**")
        }
    }
    // the reload tests of test-suite-dev-reload also cover the development support of these modules
    listOf(":micronaut-langchain4j-core", ":micronaut-langchain4j-agentic").forEach { path ->
        project(path) {
            configure<SonarExtension> {
                properties {
                    property(
                        "sonar.coverage.jacoco.xmlReportPaths",
                        listOf(projectDir, rootDir.resolve("test-suite-dev-reload"))
                            .joinToString(",") { it.resolve("build/reports/jacoco/test/jacocoTestReport.xml").path }
                    )
                }
            }
        }
    }
}


tasks {
    javadoc {
        exclude("io/micronaut/langchain4j/bedrock/**")
    }
}
