package io.github.jdcmp.codegen.bridge;

import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.codegen.CodegenProvider;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * "Holds" generated classes.
 *
 */
@ThreadSafe
public final class GeneratedClassHolder {

	/**
	 * Returns a privileged Lookup for {@link GeneratedClassHolder}.
	 *
	 * @param caller The caller's Lookup
	 * @return A privileged lookup for {@link GeneratedClassHolder}
	 */
	public static Lookup lookup(Lookup caller) {
		if (CodegenProvider.class != caller.lookupClass()) {
			throw new IllegalArgumentException("Caller is not permitted to retrieve Lookup.");
		}

		return MethodHandles.lookup();
	}

	private GeneratedClassHolder() {
		throw new AssertionError("No instances");
	}

}
