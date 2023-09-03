package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for double arrays.
 *
 * @param <T> Type of the object containing the double array
 */
public interface SerializableDoubleArrayGetter<T> extends
		DoubleArrayGetter<T>,
		SerializableGenericGetter<T, double[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableDoubleArrayGetter.of(MyClass::getSomeDoubleArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableDoubleArrayGetter<T> of(SerializableDoubleArrayGetter<T> getter) {
		return getter;
	}

}
