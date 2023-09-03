package io.github.jdcmp.codegen;

import java.lang.invoke.MethodHandles.Lookup;

/**
 * Indicates that a given {@link Lookup} has insufficient privileges to access some type.
 */
public final class UnderprivilegedLookupException extends IllegalArgumentException {

	/**
	 * Constructs an {@link UnderprivilegedLookupException} with a message containing the Lookup and type.
	 *
	 * @param lookup The lookup attempting the access
	 * @param classToBeAccessed The type to be accessed
	 * @return A new exception instance
	 */
	public static UnderprivilegedLookupException of(Lookup lookup, Class<?> classToBeAccessed) {
		String message = classToBeAccessed + " is inaccessible to Lookup: " + lookup;

		return new UnderprivilegedLookupException(message);
	}

	private UnderprivilegedLookupException(String message) {
		super(message);
	}

}
