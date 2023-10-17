package io.github.jdcmp.api.getter.primitive;

import io.github.jdcmp.api.getter.OrderingCriterion;

import java.util.function.Predicate;

/**
 * Getter for booleans.
 *
 * @param <T> Type of the object containing the boolean
 */
@FunctionalInterface
public interface BooleanGetter<T> extends OrderingCriterion<T>, Predicate<T> {

	@Override
	default int hash(T object) {
		return Boolean.hashCode(test(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return test(self) == test(other);
	}

	@Override
	default int compare(T self, T other) {
		return Boolean.compare(test(self), test(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>BooleanGetter.of(MyClass::getSomeBoolean)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @return The getter
	 */
	static <T> BooleanGetter<T> of(BooleanGetter<T> getter) {
		return getter;
	}

}
