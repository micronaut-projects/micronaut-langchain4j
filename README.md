<!-- Checklist: https://github.com/micronaut-projects/micronaut-core/wiki/New-Module-Checklist -->

# Micronaut Langchain4j

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.langchain4j/micronaut-langchain4j.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.langchain4j%22%20AND%20a:%22micronaut-langchain4j%22)
[![Build Status](https://github.com/micronaut-projects/micronaut-langchain4j/workflows/Java%20CI/badge.svg)](https://github.com/micronaut-projects/micronaut-langchain4j/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=micronaut-projects_micronaut-template&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=micronaut-projects_micronaut-template)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.micronaut.io/scans)

Micronaut Langchain4j

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-langchain4j/latest/guide/) for more information.

See the [Snapshot Documentation](https://micronaut-projects.github.io/micronaut-langchain4j/snapshot/guide/) for the current development docs.

<!-- ## Examples

Examples can be found in the [examples](https://github.com/micronaut-projects/micronaut-langchain4j/tree/master/examples) directory. -->

## Snapshots and Releases

Snapshots are automatically published to [Sonatype Snapshots](https://s01.oss.sonatype.org/content/repositories/snapshots/io/micronaut/) using [GitHub Actions](https://github.com/micronaut-projects/micronaut-langchain4j/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to Maven Central via [GitHub Actions](https://github.com/micronaut-projects/micronaut-langchain4j/actions).

Releases are completely automated. To perform a release use the following steps:

* [Publish the draft release](https://github.com/micronaut-projects/micronaut-langchain4j/releases). There should be already a draft release created, edit and publish it. The Git Tag should start with `v`. For example `v1.0.0`.
* [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-langchain4j/actions?query=workflow%3ARelease) to check it passed successfully.
* If everything went fine, [publish to Maven Central](https://github.com/micronaut-projects/micronaut-langchain4j/actions?query=workflow%3A"Maven+Central+Sync").
* Celebrate!
