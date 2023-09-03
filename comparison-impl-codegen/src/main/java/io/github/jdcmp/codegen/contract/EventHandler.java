package io.github.jdcmp.codegen.contract;


import io.github.jdcmp.api.documentation.ThreadSafe;

/**
 * An event handler may be used to intercept certain parts of the class generation process.
 */
@ThreadSafe
public interface EventHandler {

	/**
	 * Called after a generated class has been successfully instantiated.
	 *
	 * @param comparatorClass Generated comparator class
	 * @param bytes Bytes used to define the generated class
	 * @throws Exception If event handling fails; simplifies development by not forcing try around checked exceptions
	 */
	default void onClassInstantiated(Class<?> comparatorClass, byte[] bytes) throws Exception {
	}

}
