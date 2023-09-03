# Development notes for comparison-impl-codegen

## Comparator

* The `classToCompare` field could be omitted and the `GETSTATIC` calls removed.
	* This will only work for classes that can be referenced by name.
	* Special-casing VM-anonymous/hidden classes may be possible.
* Getters could be stored inside an array instead of individual fields.
	* The array must have `static final` / `@Stable` performance characteristics.
	* Special-casing JVMs with proper support may be possible.

## hashCode

### Precalculation

* The current implementation does not generate typical `hashCode` implementations. Instead, it
  places precalculated values inside the generated code.
	* Perhaps this behavior should be configurable.
* The advantages and disadvantages need to be examined more thoroughly to determine in which
  cases this optimization is worthwhile.
	* The number of calculations is reduced, but the bytecode size is probably increased.
	* Some older architectures appear to benefit significantly more than newer ones.
	  The dual-core 1.86GHz *Intel Atom N2800* from 2011 is a suitable candidate.

**Typical hashCode implementation:**

```
int multiplier = 37;
int h = 17;

h = h * multiplier + a.hashCode();
h = h * multiplier + b.hashCode();
h = h * multiplier + c.hashCode();
```

The current implementation generates code that precalculates `CONSTANT#`:

```
h(n) = h(n - 1) * multiplier + object.hashCode()

h(0) = initial * multiplier + a.hashCode()
h(1) = h(0) * multiplier + b.hashCode()
     = (initial * multiplier + a.hashCode()) * multiplier + b.hashCode()
     = initial * multiplier * multiplier + a.hashCode() * multiplier + b.hashCode()
     = initial * (multiplier * multiplier) + a.hashCode() * multiplier + b.hashCode()
     = initial * CONSTANT0 + a.hashCode() * multiplier + b.hashCode()
     = (initial * CONSTANT0) + a.hashCode() * multiplier + b.hashCode()
     = CONSTANT1 + a.hashCode() * multiplier + b.hashCode()
h(2) = h(1) * multiplier + c.hashCode()
     = (CONSTANT1 + a.hashCode() * multiplier + b.hashCode()) * multiplier + c.hashCode()
     = CONSTANT1 * multiplier + a.hashCode() * multiplier * multiplier + b.hashCode() * multiplier + c.hashCode()
     = (CONSTANT1 * multiplier) + a.hashCode() * (multiplier * multiplier) + b.hashCode() * multiplier + c.hashCode()
     = CONSTANT2 + a.hashCode() * CONSTANT0 + b.hashCode() * multiplier + c.hashCode()

CONSTANT0 = multiplier * multiplier
CONSTANT1 = CONSTANT0 * initial;
CONSTANT2 = CONSTANT1 * multiplier;
```

## Instantiation and field initialization

### Instantiation

There are two private APIs that permit instantiation of constructorless classes:

* `sun.misc.Unsafe`
* `sun.reflect.ReflectionFactory`

The `ReflectionFactory` appears to be unable to handle VM-anonymous or hidden classes in some cases.
Without doing further research, there appear to be some changes in
`jdk.internal.reflect.MethodAccessorGenerator` that could be causing this behavior:

```java
if (isConstructor) {
	asm.emitConstantPoolUTF8("newInstance");
} else {
	asm.emitConstantPoolUTF8("invoke");
}
invokeIdx = asm.cpi();
if (isConstructor) {
	asm.emitConstantPoolUTF8("([Ljava/lang/Object;)Ljava/lang/Object;");
} else {
	asm.emitConstantPoolUTF8
          ("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
}
```

### Field initialization

The generated classes contain `static final` fields that must be initialized prior to
instantiation. There are several techniques to achieve this:

* Using Reflection, `Lookup.IMPL_LOOKUP` or `sun.misc.Unsafe` to write the fields. This approach
  requires neither a static initializer nor a constructor.
* Registering parameters inside a `ThreadLocal`, then triggering a static initializer that
  retrieves them via a well known static method.
* Using `Unsafe.defineAnonymousClass` with constant pool patching.
* Using `Lookup.defineHiddenClassWithClassData` and resolving the ClassData inside a static
  initializer using `MethodHandles.classData`.

## Defining classes

* In Java 8, there is no `Lookup.defineClass`. Injecting a generated class into an existing
  `ClassLoader` requires access to a protected method. When using the `ClassLoader` in Java 8 - 16,
  either `setAccessible(true)` or `Lookup.IMPL_LOOKUP` is required.
