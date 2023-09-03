package io.github.jdcmp.codegen;


import io.github.jdcmp.api.documentation.ThreadSafe;

@ThreadSafe
interface Instantiator {

	boolean requiresConstructor();

	boolean supportsVmAnonOrHiddenClasses();

	<T> T newInstance(Class<? extends T> clazz);

}
