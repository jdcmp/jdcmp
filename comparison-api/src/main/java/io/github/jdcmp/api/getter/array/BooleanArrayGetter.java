package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.object.GenericGetter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Getter for boolean arrays.
 *
 * @param <T> Type of the object containing the boolean array
 */
@FunctionalInterface
public interface BooleanArrayGetter<T> extends OrderingCriterion<T>, GenericGetter<T, boolean[]> {

	@Override
	default int hash(T object) {
		return Arrays.hashCode(apply(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Arrays.equals(apply(self), apply(other));
	}

	@Override
	default int compare(T self, T other) {
		return BooleanArrayGetter.compare(apply(self), apply(other));
	}

	/**
	 * Compares two arrays lexicographically.
	 *
	 * @param a Left array
	 * @param b Right array
	 * @return Comparison result
	 */
	static int compare(boolean[] a, boolean[] b) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);

		if (a == b) {
			return 0;
		}

		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; ++i) {
			int result = Boolean.compare(a[i], b[i]);
			if (result != 0) {
				return result;
			}
		}

		return a.length - b.length;
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>BooleanArrayGetter.of(MyClass::getSomeBooleanArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> BooleanArrayGetter<T> of(BooleanArrayGetter<T> getter) {
		return getter;
	}

}
