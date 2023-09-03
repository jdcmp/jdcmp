package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for int arrays.
 *
 * @param <T> Type of the object containing the int array
 */
public interface SerializableIntArrayGetter<T> extends
		IntArrayGetter<T>,
		SerializableGenericGetter<T, int[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableIntArrayGetter.of(MyClass::getSomeIntArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableIntArrayGetter<T> of(SerializableIntArrayGetter<T> getter) {
		return getter;
	}

}
