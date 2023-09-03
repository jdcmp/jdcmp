# General implementation notes

This document is meant to summarize and clarify development practices.

## Java version compatibility

* The minimum Java version is 8.
* Features only present in Java 9+ must not be referenced directly. Reflective access should be
  used instead.
* Features no longer present in certain Java versions must have alternative strategies to satisfy
  all currently supported versions.

## Java implementation compatibility

* Private/internal features such as `sun.misc.Unsafe` or `jdk.internal.vm.annotation.ForceInline`
  should only be used if no suitable public replacement exists.
* Different flavors such as Oracle/Azul/OpenJDK/HotSpot/GraalVM must be taken into account.

## Serialization

* The implementation strives to separate non-`Serializable` and `Serializable` implementations.
	* Future Java versions might remove serialization.
	* It should be easier to split the implementation (e.g. another module) and perform other
	  refactorings.
	* Users who do not want serialization are free to choose slim implementations with fewer
	  interfaces and methods.

## Package structure

* Java 8 has no support for package encapsulation via modules. Using package-private access is the
  only option until Java 8 support is dropped.
* Encapsulation from clients is more important than small, homogeneous packages for internal use.
	* As few types as possible should be publicly exposed.
	* Internal implementation details must not be publicly exposed, even if it means putting
	  everything into a single package.
