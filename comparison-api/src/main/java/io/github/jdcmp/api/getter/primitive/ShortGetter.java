package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * Getter for shorts.
 *
 * @param <T> Type of the object containing the short
 */
@FunctionalInterface
public interface ShortGetter<T> extends OrderingCriterion<T> {

	/**
	 * Retrieves the short from the given object.
	 *
	 * @param object The object containing the short
	 * @return A short
	 */
	short get(T object);

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
	 * <p>Example: <code>ShortGetter.of(MyClass::getSomeLong)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> ShortGetter<T> of(ShortGetter<T> getter) {
		return getter;
	}

}
