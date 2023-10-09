package io.github.jdcmp.api.getter.object;

import io.github.jdcmp.api.getter.OrderingCriterion;

import java.util.Comparator;
import java.util.Objects;

/**
 * Getter for {@link Comparable}.
 *
 * @param <T> Type of the object containing the {@link Comparable}
 */
@FunctionalInterface
public interface ComparableGetter<T, R extends Comparable<? super R>> extends OrderingCriterion<T>, GenericGetter<T, R> {

	@Override
	default int hash(T object) {
		return Objects.hashCode(apply(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Objects.equals(apply(self), apply(other));
	}

	@Override
	default int compare(T self, T other) {
		return apply(self).compareTo(apply(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ComparableGetter.of(MyClass::getComparable)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> ComparableGetter<T, R> of(ComparableGetter<T, R> getter) {
		return getter;
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ComparableGetter.nullsFirst(MyClass::getComparable)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> OrderingCriterion<T> nullsFirst(ComparableGetter<T, R> getter) {
		return new ComparatorGetter<>(getter, Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ComparableGetter.nullsLast(MyClass::getComparable)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> OrderingCriterion<T> nullsLast(ComparableGetter<T, R> getter) {
		return new ComparatorGetter<>(getter, Comparator.nullsLast(Comparator.naturalOrder()));
	}

}
