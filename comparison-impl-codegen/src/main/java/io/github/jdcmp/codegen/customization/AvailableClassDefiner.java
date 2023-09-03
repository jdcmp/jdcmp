package io.github.jdcmp.codegen.customization;

/**
 * Configuration constants and documentation describing built-in strategies for defining classes from bytes.
 */
public enum AvailableClassDefiner {

	/**
	 * Implementation based on sun.misc.Unsafe.defineAnonymousClass. In Java versions prior to 15, it is the only way to define
	 * lightweight classes that offer better optimizations. Removed in Java 17 and replaced by hidden classes.
	 */
	VM_ANONYMOUS,
	/**
	 * Implementation based on java.lang.invoke.MethodHandles$Lookup.defineHiddenClassWithClassData. Modern, official replacement
	 * for the private sun.misc.Unsafe.defineAnonymousClass method, including an alternative to constant pool patching.
	 * Only available in Java 16+.
	 */
	LOOKUP_HIDDEN_CLASS_DATA,
	/**
	 * Implementation based on java.lang.invoke.MethodHandles$Lookup.defineHiddenClass. Modern, official replacement for the private
	 * sun.misc.Unsafe.defineAnonymousClass method, excluding constant pool patching. Only available in Java 15+.
	 */
	LOOKUP_HIDDEN,
	/**
	 * Implementation based on java.lang.invoke.MethodHandles$Lookup.defineClass. Uses ClassLoader under the hood. Only available
	 * in Java 9+.
	 */
	LOOKUP,
	/**
	 * Implementation based on java.lang.ClassLoader.defineClass. Unlike the other options, it is both official and available in old
	 * Java versions, thus it offers the greatest compatibility. It is not as lightweight as VM-anonymous or hidden classes and tied
	 * to a ClassLoader.
	 */
	CLASS_LOADER,

}
