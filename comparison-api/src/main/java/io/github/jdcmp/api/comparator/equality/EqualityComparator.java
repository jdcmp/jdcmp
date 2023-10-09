package io.github.jdcmp.api.comparator.equality;


import io.github.jdcmp.api.documentation.ThreadSafe;
import org.jetbrains.annotations.Nullable;

/**
 * <p>A comparator that does not determine {@link Comparable#compareTo(Object) ordering}, but instead provides the features of
 * {@link Object#hashCode()} and {@link Object#equals(Object)} in a consistent manner (i.e. based on the same criteria).</p>
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface EqualityComparator<T> {

	/**
	 * Calculates the given object's hash code.
	 *
	 * @param object A nullable object
	 * @return The object's hash code
	 */
	int hash(@Nullable T object);

	/**
	 * @param self  A primary object with a known type, typically <i>this</i> reference when implementing inside <i>T</i>; nullable
	 * @param other A foreign object with an unknown type, typically the argument in {@link Object#equals(Object)}; nullable
	 * @return If objects are equal true; false otherwise
	 */
	boolean areEqual(@Nullable T self, @Nullable Object other);

}
