package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for generic arrays.
 *
 * @param <T> Type of the object containing the generic array
 */
public interface SerializableGenericArrayGetter<T, R> extends GenericArrayGetter<T, R>, SerializableGenericGetter<T, R[]> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableGenericArrayGetter.of(MyClass::getSomeArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R> SerializableGenericArrayGetter<T, R> of(SerializableGenericArrayGetter<T, R> getter) {
		return getter;
	}

}
