package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for float arrays.
 *
 * @param <T> Type of the object containing the float array
 */
public interface SerializableFloatArrayGetter<T> extends
		FloatArrayGetter<T>,
		SerializableGenericGetter<T, float[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableFloatArrayGetter.of(MyClass::getSomeFloatArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableFloatArrayGetter<T> of(SerializableFloatArrayGetter<T> getter) {
		return getter;
	}

}
