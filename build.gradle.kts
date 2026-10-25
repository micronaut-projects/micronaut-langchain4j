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
}


tasks {
    javadoc {
        exclude("io/micronaut/langchain4j/bedrock/**")
    }
}
