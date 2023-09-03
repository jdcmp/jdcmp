package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * Getter for floats.
 *
 * @param <T> Type of the object containing the float
 */
@FunctionalInterface
public interface FloatGetter<T> extends OrderingCriterion<T> {

	/**
	 * Retrieves the float from the given object.
	 *
	 * @param object The object containing the float
	 * @return A float
	 */
	float get(T object);

	@Override
	default int hash(T object) {
		return Float.floatToIntBits(get(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Float.floatToIntBits(get(self)) == Float.floatToIntBits(get(other));
	}

	@Override
	default int compare(T self, T other) {
		return Float.compare(get(self), get(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>FloatGetter.of(MyClass::getSomeFloat)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> FloatGetter<T> of(FloatGetter<T> getter) {
		return getter;
	}

}
