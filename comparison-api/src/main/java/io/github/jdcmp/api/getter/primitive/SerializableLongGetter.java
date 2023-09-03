package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for longs.
 *
 * @param <T> Type of the object containing the long
 */
public interface SerializableLongGetter<T> extends LongGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>LongGetter.of(MyClass::getSomeLong)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableLongGetter<T> of(SerializableLongGetter<T> getter) {
		return getter;
	}

}
