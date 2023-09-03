package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for floats.
 *
 * @param <T> Type of the object containing the float
 */
public interface SerializableFloatGetter<T> extends FloatGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableFloatGetter.of(MyClass::getSomeFloat)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableFloatGetter<T> of(SerializableFloatGetter<T> getter) {
		return getter;
	}

}
