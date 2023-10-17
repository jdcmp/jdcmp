package io.github.jdcmp.api.getter.object;

import io.github.jdcmp.api.getter.SerializableEqualityCriterion;

/**
 * Serializable getter for generic types.
 *
 * @param <T> Type of the object containing the generic type
 */
public interface SerializableGenericGetter<T, R> extends GenericGetter<T, R>, SerializableEqualityCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableGenericGetter.of(MyClass::getSomething)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R> SerializableGenericGetter<T, R> of(SerializableGenericGetter<T, R> getter) {
		return getter;
	}

}
