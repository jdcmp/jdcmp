package io.github.jdcmp.codegen.contract;


import io.github.jdcmp.api.documentation.ThreadSafe;

import java.lang.invoke.MethodHandles.Lookup;

/**
 * Provides {@link Lookup} instances for a given type.
 */
@ThreadSafe
public interface LookupFactory {

	/**
	 * Retrieves a {@link Lookup} for the given type.
	 *
	 * @param clazz The type whose Lookup is desired
	 * @return A lookup
	 */
	Lookup lookupFor(Class<?> clazz);

}
