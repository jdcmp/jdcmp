# comparison-api

This document describes the API project `comparison-api`. Concrete implementations such as
`comparison-impl-codegen` are documented elsewhere.

## Hash Parameters

The initial value and the multiplier used in `hash(Object)` may be customized by using the
builder's `hashParameters(HashParameters)` method.

Example: `.hashParameters(HashParameters.of(17, 37))`

## Comparator without comparison criteria

In case the getters are supplied dynamically to the builder, or in case a developer accidentally
deletes the relevant lines, a fallback may be used:

* `requireAtLeastOneGetter`: No fallback, forces the developer to supply at least one criterion.
* `fallbackToIdentity`: Falls back to an identity comparison (e.g. `x == y`). The resulting
  comparator has inconsistent `areEqual(Object, Object)` and `compareTo(Object)` methods.
* `fallbackToNaturalOrdering`: Falls back to the natural order of a `Comparable` type. May cause
  infinite recursion if used incorrectly (if the comparator is used to implement the very same
  fallback it calls).

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
* May be used in cases where an implementation providing the comparator requires access to an
  otherwise inaccessible type.
