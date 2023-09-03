package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Serializable {@link ComparableArrayGetter}.
 *
 * @param <T> Type whose instances can be compared
 * @param <R> Type of the criterion
 */
public interface SerializableComparableArrayGetter<T, R extends Comparable<? super R>> extends
		ComparableArrayGetter<T, R>,
		SerializableGenericArrayGetter<T, R>,
		SerializableOrderingCriterion<T> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableComparableArrayGetter.of(MyClass::getSomeComparableArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 * @param <R> Type of the criterion
	 */
	static <T, R extends Comparable<? super R>> SerializableComparableArrayGetter<T, R> of(SerializableComparableArrayGetter<T, R> getter) {
		return getter;
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableComparableArrayGetter.nullsFirst(MyClass::getSomeComparableArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 * @param <R> Type of the criterion
	 */
	static <T, R extends Comparable<? super R>> SerializableOrderingCriterion<T> nullsFirst(SerializableComparableArrayGetter<T, R> getter) {
		return new SerializableComparatorGetter<>(getter, nullsFirst());
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>SerializableComparableArrayGetter.nullsLast(MyClass::getSomeComparableArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 * @param <R> Type of the criterion
	 */
	static <T, R extends Comparable<? super R>> SerializableOrderingCriterion<T> nullsLast(SerializableComparableArrayGetter<T, R> getter) {
		return new SerializableComparatorGetter<>(getter, nullsLast());
	}

	/**
	 * Orders nulls first if either the array is null or one of its elements is null.
	 *
	 * @return Comparator
	 * @param <R> Type of the criterion
	 */
	static <R extends Comparable<? super R>> Comparator<R[]> nullsFirst() {
		Comparator<R> elementComparator = Comparator.nullsFirst(Comparator.naturalOrder());

		return Comparator.nullsFirst(compareElementsBy(elementComparator));
	}

	/**
	 * Orders nulls last if either the array is null or one of its elements is null.
	 *
	 * @return Comparator
	 * @param <R> Type of the criterion
	 */
	static <R extends Comparable<? super R>> Comparator<R[]> nullsLast() {
		Comparator<R> elementComparator = Comparator.nullsLast(Comparator.naturalOrder());

		return Comparator.nullsLast(compareElementsBy(elementComparator));
	}

	/**
	 * Creates a lexicographical comparator using the given array element comparator. Does not accept null array references.
	 * The comparator is {@link Serializable}.
	 *
	 * @param elementComparator Comparator for array elements
	 * @return Comparator
	 * @param <R> Array component type
	 */
	static <R extends Comparable<? super R>> Comparator<R[]> compareElementsBy(Comparator<R> elementComparator) {
		return (Comparator<R[]> & Serializable) (a, b) -> compareLexicographically(a, b, elementComparator);
	}

	/**
	 * Compares two arrays lexicographically.
	 *
	 * @param a Left array
	 * @param b Right array
	 * @return Comparison result
	 * @param <C> Comparable type with natural ordering
	 */
	static <C extends Comparable<? super C>> int compareLexicographically(C[] a, C[] b) {
		return ComparableArrayGetter.compareLexicographically(a, b);
	}

	/**
	 * Compares two arrays lexicographically.
	 *
	 * @param a Left array
	 * @param b Right array
	 * @param comparator Comparator
	 * @return Comparison result
	 * @param <C> Generic array component type
	 */
	static <C> int compareLexicographically(C[] a, C[] b, Comparator<? super C> comparator) {
		return ComparableArrayGetter.compareLexicographically(a, b, comparator);
	}

}
