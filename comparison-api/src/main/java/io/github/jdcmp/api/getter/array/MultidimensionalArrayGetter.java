package io.github.jdcmp.api.getter.array;

import java.util.Arrays;

/**
 * Getter for multi-dimensional arrays.
 *
 * @param <T> Type of the object containing the multi-dimensional array
 */
@FunctionalInterface
public interface MultidimensionalArrayGetter<T, R> extends GenericArrayGetter<T, R> {

	@Override
	default int hash(T object) {
		return Arrays.deepHashCode(apply(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Arrays.deepEquals(apply(self), apply(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>MultidimensionalArrayGetter.of(MyClass::getSomeMultidimensionalArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R> MultidimensionalArrayGetter<T, R> of(MultidimensionalArrayGetter<T, R> getter) {
		return getter;
	}

}
