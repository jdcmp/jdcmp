package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for short arrays.
 *
 * @param <T> Type of the object containing the short array
 */
public interface SerializableShortArrayGetter<T> extends
		ShortArrayGetter<T>,
		SerializableGenericGetter<T, short[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableShortArrayGetter.of(MyClass::getSomeShortArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableShortArrayGetter<T> of(SerializableShortArrayGetter<T> getter) {
		return getter;
	}

}
