# TODOs

This document is used to accumulate ongoing tasks and ideas for future improvements.

## Documentation

* Improve and expand Javadoc.
* Improve and expand Markdown.

## Features

* Add more events for `EventHandler`.
* Add Logging.

## Optimizations and research

* Publish source code and results of benchmarks.
* Use vectorized array comparison on Java 9+.
	* A different implementation could be provided for a multi-release JAR.
	* The current implementation could check whether the methods are available at runtime and call
	  them using Reflection/MethodHandle.
* Store getters inside a `@Stable`-semantics array.
* Investigate `ClassOption.NESTMATE`.

## Tests

* Add more combinations of ClassDefiners, Instantiators and other customization.
* Publish docker image source code for automated testing of many JVMs.
