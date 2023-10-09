package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * Getter for chars.
 *
 * @param <T> Type of the object containing the char
 */
@FunctionalInterface
public interface CharGetter<T> extends OrderingCriterion<T> {

	/**
	 * Retrieves the char from the given object.
	 *
	 * @param object The object containing the char
	 * @return A char
	 */
	char get(T object);

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
	 * <p>Example: <code>CharGetter.of(MyClass::getSomeChar)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> CharGetter<T> of(CharGetter<T> getter) {
		return getter;
	}

}
