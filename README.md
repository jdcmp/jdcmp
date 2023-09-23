# jdcmp - Declarative Comparisons

jdcmp is an open-source library for Java™ that helps developers implement consistent `hashCode()`,
`equals(Object)` and `compareTo(Object)` methods in a declarative, programmatic and compile-safe manner.

## Features

* Consistent implementations of `hashCode()`, `equals(Object)` and `compareTo(Object)`
* Declaration instead of implementation
* Drop-in replacement for `Comparator<T>`
* Null-safety (optional feature)
* Supports `Serializable` (optional feature)
* Faster than many alternatives such as `Comparator.comparing`

## Compatibility

The following table lists runtimes that are known to be compatible with the default implementation 
`comparison-impl-codegen`.  

| Flavor              | Versions   |
|---------------------|------------|
| OpenJDK             | 8 - 22     |
| IBM Semeru (OpenJ9) | 17 - 20    |
| GraalVM CE          | 17, 20, 21 |
| Oracle GraalVM      | 17, 20, 21 |

The following features are untested:

* Creating comparators for [Records](https://docs.oracle.com/en/java/javase/20/language/records.html).
* Creating comparators for VM-anonymous or hidden classes.

## Adding dependency to a project

### Maven

Check the [Releases page](https://github.com/jdcmp/jdcmp/releases) and replace
`<version>VERSION</version>` with an appropriate entry.

**Quickstart**

Add this to `<dependencies>` inside your POM (e.g. `pom.xml`) and replace `VERSION`:

```xml
<dependency>
    <groupId>io.github.jdcmp</groupId>
    <artifactId>comparison-impl-codegen</artifactId>
    <version>VERSION</version> <!-- See https://github.com/jdcmp/jdcmp/releases -->
</dependency>
```

**With dependencyManagement**

Users of `<dependencyManagement>` may copy [some XML](docs/user/adding-the-maven-dependency.md).

## Dependencies

The API project `comparison-api` requires no dependencies. The default implementation
`comparison-impl-codegen` requires the following dependencies:

* `org.ow2.asm:asm` ([shaded](https://maven.apache.org/plugins/maven-shade-plugin/))
* `org.ow2.asm:asm-commons` ([shaded](https://maven.apache.org/plugins/maven-shade-plugin/))
* `org.ow2.asm:asm-tree` ([shaded](https://maven.apache.org/plugins/maven-shade-plugin/))
* `org.jetbrains:annotations`

Licensing information is available [here](docs/licensing).

## Examples

### hashCode & equals

The example below demonstrates the implementation of `hashCode()` and `equals(Object)`.

```java
class Person {

    private static final EqualityComparator<Person> COMPARATOR = Comparators.equality()
        .nonSerializable()    
        .requireAtLeastOneGetter(Person.class)
        .use(ObjectGetter.of(Person::getFirstName))
        .use(ObjectGetter.of(Person::getLastName))
        .build();
  
    private String firstName;
  
    private String lastName;
  
    public String getFirstName() {
        return firstName;
    }
  
    public String getLastName() {
        return lastName;
    }
  
    public int hashCode() {
        return COMPARATOR.hash(this);
    }
  
    public boolean equals(Object obj) {
        return COMPARATOR.areEqual(this, obj);
    }

}
```

### compareTo

The example below demonstrates the implementation of `hashCode()`, `equals(Object)` and
`compareTo(Object)`.


```java
class Person implements Comparable<Person> {
  
    private static final OrderingComparator<Person> COMPARATOR = Comparators.ordering()
        .nonSerializable()
        .requireAtLeastOneGetter(Person.class)
        .use(ComparableGetter.of(Person::getFirstName))
        .use(ComparableGetter.of(Person::getLastName))
        .build();

    private String firstName;

    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int hashCode() {
        return COMPARATOR.hash(this);
    }

    public boolean equals(Object obj) {
        return COMPARATOR.areEqual(this, obj);
    }

    public int compareTo(Person other) {
        return COMPARATOR.compare(this, other);
    }

}
```

### Null-safety

By default, `null` method arguments are not permitted in `compare(Object, Object)`.

**Handling nullable arguments**

The following example uses the builder's optional `nullsFirst()` method to create a comparator
that accepts `null` arguments in its `compare(Object, Object)` method.

```java
Comparators.ordering()
    .nonSerializable()
    .requireAtLeastOneGetter(X.class)
    .use(ComparableArrayGetter.of(X::getStringArray))
    .nullsFirst()
    .build();
```

Note, however, that this does not affect the ability of each criterion to handle `null`. If
`X::getStringArray` returns `null`, or if the returned array contains `null` elements, a
`NullPointerException` is still thrown.

**Handling nullable criteria**

When using multi-element criteria such as `String[]`, there are two cases where `null` must be
handled:

* The `String[]` reference could be `null`.
* A non-null `String[]` could still contain `null` elements.

The following example uses a `null`-safe getter:

```java
Comparators.ordering()
    .nonSerializable()
    .requireAtLeastOneGetter(X.class)
    .use(ComparableArrayGetter.nullsFirst(X::getStringArray))
    .nullsFirst()
    .build();
```

## Main types

The following sections describe the most important types.

**hashCode, equals, compareTo**

* `Comparators`: Main API for obtaining instances
* `EqualityComparator<T>`: Comparator for `hashCode` & `equals`
* `OrderingComparator<T>`: Comparator for `hashCode`, `equals` and `compareTo`
* `EqualityCriterion<T>`: Provides `hashCode` & `equals` based on a single property of `T`
* `OrderingCriterion<T>`: Provides `hashCode`, `equals` & `compareTo` based on a single property
  of a sortable `T` (e.g. `String`, `int`, `long[]`, ...)

**Serialization**

* `SerializationSupport`: Static utilities for serialization
* `SerializableEqualityComparator<T>`: Serializable equivalent of `EqualityComparator<T>`
* `SerializableOrderingComparator<T>`: Serializable equivalent of `OrderingComparator<T>`
* `Serializable*Getter`: Serializable equivalents of getters

**ServiceLoader**

* `ComparatorProviders`: Static factory to obtain concrete implementations such as
  `comparison-impl-codegen`
* `ComparatorProvider`: Interface whose implementation lies in `comparison-impl-codegen` and
  similar implementations. A provider creates comparators and is called via `Comparators` upon
  calling `build()` on one of the builders.

## Managing Provider instances

### Managing lifecycle

Methods such as `ComparatorProviders.load()` are called from various places to obtain a default
`ComparatorProvider`, one example being the deserialization process. Frequent
invocations may cause a lot of instance creation and garbage collection overhead. Therefore,
it is recommended to manage the lifecycle of a specific `ComparatorProvider` via some
application-specific mechanism. It is recommended to use an existing IoC-Container in environments
such as `Spring`. Here is a plain Java solution with no frameworks:

```java
class MyApplication {

    private static final ComparatorProvider PROVIDER = initializeProvider();
  
    public static void main(String[] args) {
        // ...
    }
  
    private static ComparatorProvider initializeProvider() {
        CodegenProvider provider = ComparatorProviders.load(CodegenProvider.class);
        provider.setSerializationMode(AvailableSerializationMode.COMPATIBLE);
    
        ComparatorProviders.setDefaultProvider(provider);
        ComparatorProviders.setSerializationProvider(provider);
    
        return provider;
    }

}
```

### Loading a provider

There are two ways to load a `ComparatorProvider`.

**Automatic instantiation**

This method scans the class path for implementations of `comparison-api` and returns a random
provider. Implementations such as `comparison-impl-codegen` are automatically detected, if they
provide a service configuration in `src/main/resources/META-INF.services`.

```java
ComparatorProvider provider = ComparatorProviders.load();
```

**Explicit instantiation**

This method loads a specific provider. The provider must have a public no-args constructor.

```java
MyComparatorProvider provider = ComparatorProviders.load(MyComparatorProvider.class);
```

## Serialization

Implementations are designed to be compatible with Java's serialization mechanism. All types
have *Serializable* counterparts (i.e. `OrderingComparator<T>` is extended by
`SerializableOrderingComparator<T>`). This gives users free choice over whether they wish to
introduce serialization and its security implications into their projects, or use slimmer,
safer implementations.

All supplied information such as getters must be serializable as well. The example below
demonstrates how to create the serializable lambda `Person::getFirstName` by using the helper
method `SerializableComparableGetter.of(getter)`:

```java
class Person implements Comparable<Person> {

    private static final SerializableOrderingComparator<Person> COMPARATOR = Comparators.ordering()
        .serializable()
        .requireAtLeastOneGetter(Person.class)
        .use(SerializableComparableGetter.of(Person::getFirstName))
        .use(SerializableComparableGetter.of(Person::getLastName))
        .build();
  
    private String firstName;
  
    private String lastName;
  
    public String getFirstName() {
        return firstName;
    }
  
    public String getLastName() {
        return lastName;
    }
  
    public int hashCode() {
        return COMPARATOR.hash(this);
    }
  
    public boolean equals(Object obj) {
        return COMPARATOR.areEqual(this, obj);
    }
  
    public int compareTo(Person other) {
        return COMPARATOR.compareTo(this, other);
    }

}
```

## More documentation

The [docs directory](docs) contains further information about licenses, configuration,
implementation and development.

## Contributing

Please file bug reports, feature requests or questions via GitHub issues, or contact me privately
via e-mail at `jari.schaefer@gmail.com`.

## About

This project is based on smaller, less complex implementations that I have been using in some of my
private projects. It serves as a learning environment for several things such as API design, Java
version & flavor compatibility, Java Object Serialization, internal APIs (e.g. `sun.*`),
optimizations such as `static final`, `@Stable`, `-XX:+TrustFinalNonStaticFields`, bytecode
generation and class loading & instantiation.

## License

The project is available under the MIT license. See [LICENSE.txt](LICENSE.txt) or
[LICENSING docs](docs/licensing) for details.
