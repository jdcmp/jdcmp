package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * Getter for bytes.
 *
 * @param <T> Type of the object containing the byte
 */
@FunctionalInterface
public interface ByteGetter<T> extends OrderingCriterion<T> {

	/**
	 * Retrieves the byte from the given object.
	 *
	 * @param object The object containing the byte
	 * @return A byte
	 */
	byte get(T object);

	@Override
	default int hash(T object) {
		return get(object);
	}

	@Override
	default boolean areEqual(T self, T other) {
		return get(self) == get(other);
	}

	@Override
	default int compare(T self, T other) {
		return get(self) - get(other);
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ByteGetter.of(MyClass::getSomeByte)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> ByteGetter<T> of(ByteGetter<T> getter) {
		return getter;
	}

}
