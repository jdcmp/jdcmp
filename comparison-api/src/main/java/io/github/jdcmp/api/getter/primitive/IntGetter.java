package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

import java.util.function.ToIntFunction;

/**
 * Getter for ints.
 *
 * @param <T> Type of the object containing the int
 */
@FunctionalInterface
public interface IntGetter<T> extends OrderingCriterion<T>, ToIntFunction<T> {

	@Override
	default int hash(T object) {
		return applyAsInt(object);
	}

	@Override
	default boolean areEqual(T self, T other) {
		return applyAsInt(self) == applyAsInt(other);
	}

	@Override
	default int compare(T self, T other) {
		return Integer.compare(applyAsInt(self), applyAsInt(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>IntGetter.of(MyClass::getSomeInt)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> IntGetter<T> of(IntGetter<T> getter) {
		return getter;
	}

}
