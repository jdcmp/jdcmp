package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for doubles.
 *
 * @param <T> Type of the object containing the double
 */
public interface SerializableDoubleGetter<T> extends DoubleGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableDoubleGetter.of(MyClass::getSomeDouble)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableDoubleGetter<T> of(SerializableDoubleGetter<T> getter) {
		return getter;
	}

}
