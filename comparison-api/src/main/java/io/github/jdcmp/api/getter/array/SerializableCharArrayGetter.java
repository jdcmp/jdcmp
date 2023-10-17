package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for char arrays.
 *
 * @param <T> Type of the object containing the char array
 */
public interface SerializableCharArrayGetter<T> extends
		CharArrayGetter<T>,
		SerializableGenericGetter<T, char[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableCharArrayGetter.of(MyClass::getSomeCharArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableCharArrayGetter<T> of(SerializableCharArrayGetter<T> getter) {
		return getter;
	}

}
