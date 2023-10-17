package io.github.jdcmp.api.getter.object;

import io.github.jdcmp.api.getter.EqualityCriterion;

import java.util.Objects;
import java.util.function.Function;

/**
 * Getter for generic types.
 *
 * @param <T> Type of the object containing the generic type
 */
@FunctionalInterface
public interface GenericGetter<T, R> extends EqualityCriterion<T>, Function<T, R> {

	@Override
	default int hash(T object) {
		Object value = apply(object);

		return Objects.hashCode(value);
	}

	@Override
	default boolean areEqual(T self, T other) {
		R valueSelf = apply(self);
		R valueOther = apply(other);

		return Objects.equals(valueSelf, valueOther);
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>GenericGetter.of(MyClass::getSomething)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R> GenericGetter<T, R> of(GenericGetter<T, R> getter) {
		return getter;
	}

}
