package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

import java.util.function.ToLongFunction;

/**
 * Getter for longs.
 *
 * @param <T> Type of the object containing the long
 */
@FunctionalInterface
public interface LongGetter<T> extends OrderingCriterion<T>, ToLongFunction<T> {

	@Override
	default int hash(T object) {
		return Long.hashCode(applyAsLong(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return applyAsLong(self) == applyAsLong(other);
	}

	@Override
	default int compare(T self, T other) {
		return Long.compare(applyAsLong(self), applyAsLong(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>LongGetter.of(MyClass::getSomeLong)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> LongGetter<T> of(LongGetter<T> getter) {
		return getter;
	}

}
