# Benchmarking

This document describes benchmarking.

## JMH

There are [JMH](https://github.com/openjdk/jmh) benchmarks that allow performance comparisons
between different implementations such as IDE-generated hashCode, Apache Commons HashBuilder,
Guava ComparisonChain and standard JDK Comparator.comparing. However, these benchmarks are
not publicly available yet.

## Thoughts

Benchmarking `hashCode()` and similar methods is a good start, but it most likely does not cover
many interesting differences such as class loading overhead or code cache size.
