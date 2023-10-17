package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.object.GenericGetter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Getter for byte arrays.
 *
 * @param <T> Type of the object containing the byte array
 */
@FunctionalInterface
public interface ByteArrayGetter<T> extends OrderingCriterion<T>, GenericGetter<T, byte[]> {

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
		return ByteArrayGetter.compare(apply(self), apply(other));
	}

	/**
	 * Compares two arrays lexicographically.
	 *
	 * @param a Left array
	 * @param b Right array
	 * @return Comparison result
	 */
	static int compare(byte[] a, byte[] b) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);

		if (a == b) {
			return 0;
		}

		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; ++i) {
			int result = Byte.compare(a[i], b[i]);
			if (result != 0) {
				return result;
			}
		}

		return a.length - b.length;
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ByteArrayGetter.of(MyClass::getSomeByteArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> ByteArrayGetter<T> of(ByteArrayGetter<T> getter) {
		return getter;
	}

}
