import org.sonarqube.gradle.SonarExtension

plugins {
    id("io.micronaut.build.internal.docs")
    id("io.micronaut.build.internal.quality-reporting")
}

repositories {
    mavenCentral()
}

micronautBuild {
    javaVersion = 21
}

if (System.getenv("SONAR_TOKEN") != null) {
    configure<SonarExtension> {
        properties {
            property("sonar.exclusions", "**/example/**")
        }
    }
}
