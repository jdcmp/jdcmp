package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for byte arrays.
 *
 * @param <T> Type of the object containing the byte array
 */
public interface SerializableByteArrayGetter<T> extends
		ByteArrayGetter<T>,
		SerializableGenericGetter<T, byte[]>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ByteArrayGetter.of(MyClass::getSomeByteArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableByteArrayGetter<T> serializable(SerializableByteArrayGetter<T> getter) {
		return getter;
	}

}
