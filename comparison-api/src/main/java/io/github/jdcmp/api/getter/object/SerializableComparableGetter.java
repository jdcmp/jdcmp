package io.github.jdcmp.api.getter.object;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

import java.util.Comparator;

/**
 * Serializable getter for {@link Comparable}.
 *
 * @param <T> Type of the object containing the {@link Comparable}
 */
public interface SerializableComparableGetter<T, R extends Comparable<? super R>> extends
		ComparableGetter<T, R>,
		SerializableGenericGetter<T, R>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableComparableGetter.of(MyClass::getComparable)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> SerializableComparableGetter<T, R> of(SerializableComparableGetter<T, R> getter) {
		return getter;
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableComparableGetter.nullsFirst(MyClass::getComparable)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> SerializableOrderingCriterion<T> nullsFirst(SerializableComparableGetter<T, R> getter) {
		return new SerializableComparatorGetter<>(getter, Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableComparableGetter.nullsLast(MyClass::getComparable)</code></p>
	 *
	 * @param getter The getter
	 * @param <T>    Type whose instances can be compared using the getter
	 * @param <R>    Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> SerializableOrderingCriterion<T> nullsLast(SerializableComparableGetter<T, R> getter) {
		return new SerializableComparatorGetter<>(getter, Comparator.nullsLast(Comparator.naturalOrder()));
	}

}
