package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

import java.util.function.ToDoubleFunction;

/**
 * Getter for doubles.
 *
 * @param <T> Type of the object containing the double
 */
@FunctionalInterface
public interface DoubleGetter<T> extends OrderingCriterion<T>, ToDoubleFunction<T> {

	@Override
	default int hash(T object) {
		return Double.hashCode(applyAsDouble(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Double.doubleToLongBits(applyAsDouble(self)) == Double.doubleToLongBits(applyAsDouble(other));
	}

	@Override
	default int compare(T self, T other) {
		return Double.compare(applyAsDouble(self), applyAsDouble(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>DoubleGetter.of(MyClass::getSomeDouble)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> DoubleGetter<T> of(DoubleGetter<T> getter) {
		return getter;
	}

}
