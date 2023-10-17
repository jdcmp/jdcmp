package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Serializable getter for bytes.
 *
 * @param <T> Type of the object containing the byte
 */
public interface SerializableByteGetter<T> extends ByteGetter<T>, SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableByteGetter.of(MyClass::getSomeByte)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableByteGetter<T> of(SerializableByteGetter<T> getter) {
		return getter;
	}

}
