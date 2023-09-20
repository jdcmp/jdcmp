package io.github.jdcmp.codegen.bridge;

import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.codegen.CodegenProvider;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * "Holds" generated classes.
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
		checkAccess(caller);

		return MethodHandles.lookup();
	}

	private static void checkAccess(Lookup caller) {
		Class<?> lookupClass = caller.lookupClass();

		if (CodegenProvider.class != lookupClass || lacksAccess(caller)) {
			throw new IllegalArgumentException("Caller is not permitted to retrieve Lookup.");
		}
	}

	private static boolean lacksAccess(Lookup caller) {
		return (caller.lookupModes() & Lookup.PRIVATE) != Lookup.PRIVATE;
	}

	private GeneratedClassHolder() {
		throw new AssertionError("No instances");
	}

}
