package io.github.jdcmp.api.getter;


import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;

/**
 * A single criterion, such as a field inside a class, used to determine equality as part of an {@link EqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface EqualityCriterion<T> {

	/**
	 * Calculates the given object's hash code.
	 *
	 * @param object An object
	 * @return The object's hash code.
	 */
	int hash(T object);

	/**
	 * @param self  A primary object, typically <i>this</i> reference when implementing inside <i>T</i>
	 * @param other A secondary object
	 * @return If objects are equal true; false otherwise
	 */
	boolean areEqual(T self, T other);

}
