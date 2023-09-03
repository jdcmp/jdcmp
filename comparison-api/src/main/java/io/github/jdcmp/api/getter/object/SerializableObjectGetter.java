package io.github.jdcmp.api.getter.object;

import io.github.jdcmp.api.getter.SerializableEqualityCriterion;

/**
 * Serializable getter for {@link Object}.
 *
 * @param <T> Type of the object containing the {@link Object}
 */
public interface SerializableObjectGetter<T> extends ObjectGetter<T>, SerializableEqualityCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableObjectGetter.of(MyClass::getSomeObject)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> SerializableObjectGetter<T> of(SerializableObjectGetter<T> getter) {
		return getter;
	}

}
