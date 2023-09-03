package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for boolean arrays.
 *
 * @param <T> Type of the object containing the boolean array
 */
public interface SerializableBooleanArrayGetter<T> extends
		BooleanArrayGetter<T>,
		SerializableGenericGetter<T, boolean[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableBooleanArrayGetter.of(MyClass::getSomeBooleanArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableBooleanArrayGetter<T> of(SerializableBooleanArrayGetter<T> getter) {
		return getter;
	}

}
