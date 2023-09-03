package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for long arrays.
 *
 * @param <T> Type of the object containing the long array
 */
public interface SerializableLongArrayGetter<T> extends
		LongArrayGetter<T>,
		SerializableGenericGetter<T, long[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableLongArrayGetter.of(MyClass::getSomeLongArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableLongArrayGetter<T> of(SerializableLongArrayGetter<T> getter) {
		return getter;
	}

}
