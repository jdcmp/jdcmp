package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;

/**
 * Serializable getter for {@link Object} arrays.
 *
 * @param <T> Type of the object containing the {@link Object} array
 */
public interface SerializableObjectArrayGetter<T> extends
		ObjectArrayGetter<T>,
		SerializableGenericGetter<T, Object[]>,
		SerializableEqualityCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableObjectArrayGetter.of(MyClass::getSomeObjectArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> SerializableObjectArrayGetter<T> of(SerializableObjectArrayGetter<T> getter) {
		return getter;
	}

}
