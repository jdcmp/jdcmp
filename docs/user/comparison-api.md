# comparison-api

This document describes the concrete API project `comparison-api`. Concrete implementations such as
`comparison-impl-codegen` are documented elsewhere.

## Strict types

During the build process, the builder may be instructed to use strict types by invoking
`strictTypes(true)` before calling `build()`. The strict types flag enforces strict type checking
in methods such as `EqualityComparator#hash(Object)`.

*Strict type checking* causes arguments to be checked at runtime for type violations. The object
given to the `hash(Object)` method will be cast to the expected type *T* of
`EqualityComparator<T>`.

## Building instances

**build()**

* Uses a default `ComparatorProvider` by searching for implementations.
* Uses a default `Lookup` access context.
* It is recommended to set default providers via `ComparatorProviders` to avoid frequent instance
creation and garbage collection overhead.

**build(ComparatorProvider)**

* Uses the given provider. 
* Uses a default `Lookup` access context.

**build(ComparatorProvider, Lookup)**

* Uses the given provider and lookup.
* May be used in cases where an implementation providing the comparator requires access to a type.
