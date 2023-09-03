package io.github.jdcmp.api.getter.array;

import java.util.Arrays;

/**
 * Getter for {@link Object} arrays.
 *
 * @param <T> Type of the object containing the {@link Object} array
 */
@FunctionalInterface
public interface ObjectArrayGetter<T> extends GenericArrayGetter<T, Object> {

	@Override
	default int hash(T object) {
		return Arrays.hashCode(apply(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Arrays.equals(apply(self), apply(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ObjectArrayGetter.of(MyClass::getSomeObjectArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> ObjectArrayGetter<T> of(ObjectArrayGetter<T> getter) {
		return getter;
	}

}
