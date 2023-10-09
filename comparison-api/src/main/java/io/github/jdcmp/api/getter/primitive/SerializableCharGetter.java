package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for chars.
 *
 * @param <T> Type of the object containing the char
 */
public interface SerializableCharGetter<T> extends CharGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableCharGetter.of(MyClass::getSomeChar)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableCharGetter<T> of(SerializableCharGetter<T> getter) {
		return getter;
	}

}
