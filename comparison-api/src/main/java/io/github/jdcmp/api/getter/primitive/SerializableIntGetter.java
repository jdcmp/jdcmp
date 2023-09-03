package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for ints.
 *
 * @param <T> Type of the object containing the int
 */
public interface SerializableIntGetter<T> extends IntGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableIntGetter.of(MyClass::getSomeInt)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableIntGetter<T> of(SerializableIntGetter<T> getter) {
		return getter;
	}

}
