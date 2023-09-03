package io.github.jdcmp.codegen.customization;

/**
 * Configuration constants and documentation describing instantiation strategies for the generated classes.
 */
public enum AvailableInstantiator {

	/**
	 * Implementation based on regular constructor invocation. This strategy requires the class
	 * to implement a constructor, thus increasing the size of the bytecode.
	 */
	CONSTRUCTOR,
	/**
	 * Implementation based on sun.misc.Unsafe.allocateInstance. This strategy does not require
	 * the class to implement a constructor.
	 */
	UNSAFE,
	/**
	 * Implementation based on sun.reflect.ReflectionFactory.newConstructorForSerialization.
	 * This strategy does not require the class to impelement a constructor.
	 */
	REFLECTION_FACTORY,

}
