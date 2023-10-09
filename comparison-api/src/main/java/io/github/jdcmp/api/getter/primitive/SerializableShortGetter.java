package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for shorts.
 *
 * @param <T> Type of the object containing the short
 */
public interface SerializableShortGetter<T> extends ShortGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableShortGetter.of(MyClass::getSomeLong)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableShortGetter<T> of(SerializableShortGetter<T> getter) {
		return getter;
	}

}
