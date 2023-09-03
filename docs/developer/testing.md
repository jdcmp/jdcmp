# Testing

This document describes testing.

## JUnit

Modules such as `comparison-api` and `comparison-impl-codegen` contain some tests, but the bulk of
features is tested using the separate `comparison-test` module.

## JVM Tests

In order to improve compatibility of internal features (e.g. `sun.reflect.ReflectionFactory`), all
tests should be run on as many JVMs as possible. There is a Docker Image that takes care of this,
however, it has not been made publicly available yet.

The idea is simple:
1. Collect a number of JVMs and place them inside a directory.
2. Build the project once with any JVM.
3. For each JVM, run `mvn verify` in the `comparison-test` module.
