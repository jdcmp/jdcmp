package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for booleans.
 *
 * @param <T> Type of the object containing the boolean
 */
public interface SerializableBooleanGetter<T> extends BooleanGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableBooleanGetter.of(MyClass::getSomeBoolean)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableBooleanGetter<T> of(SerializableBooleanGetter<T> getter) {
		return getter;
	}

}
