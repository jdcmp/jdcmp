# Updating dependencies

This document describes keeping dependencies up to date.

## Dependency management

The following rules keep the POM clean:

* Every version has its own property in `<properties>`.
* `<version>` appears only in `<dependencyManagement>` or `<pluginManagement>`.
* `<version>` references a property, not a plain value.

## Checking for dependency updates

* Mockito is incompatible with Java 8 in recent versions. Ignore the related messages.

`mvn versions:display-dependency-updates`

## Checking for plugin updates

`mvn versions:display-plugin-updates`
