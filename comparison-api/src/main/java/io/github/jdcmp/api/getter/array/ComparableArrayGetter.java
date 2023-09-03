package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.OrderingCriterion;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Getter for {@link Comparable} arrays.
 *
 * @param <T> Type of the object containing the {@link Comparable} array
 */
@FunctionalInterface
public interface ComparableArrayGetter<T, R extends Comparable<? super R>> extends GenericArrayGetter<T, R>, OrderingCriterion<T> {

	@Override
	default int hash(T object) {
		return Arrays.hashCode(apply(object));
	}

	@Override
	default boolean areEqual(T self, T other) {
		return Arrays.equals(apply(self), apply(other));
	}

	@Override
	default int compare(T self, T other) {
		return compareLexicographically(apply(self), apply(other));
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ComparableArrayGetter.of(MyClass::getSomeComparableArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T> Type whose instances can be compared using the getter
	 * @param <R> Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> ComparableArrayGetter<T, R> of(ComparableArrayGetter<T, R> getter) {
		return getter;
	}

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ComparableArrayGetter.nullsFirst(MyClass::getSomeComparableArray)</code></p>
	 *
	 * @param getter The getter
	 * @param <T> Type whose instances can be compared using the getter
	 * @param <R> Type of the criterion
	 * @return The getter
	 */
	static <T, R extends Comparable<? super R>> OrderingCriterion<T> nullsFirst(ComparableArrayGetter<T, R> getter) {
		return new ComparatorGetter<>(getter, nullsFirst());
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
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ComparableArrayGetter.nullsLast(MyClass::getSomeComparableArray)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 * @param <R> Comparable type with natural ordering
	 */
	static <T, R extends Comparable<? super R>> OrderingCriterion<T> nullsLast(ComparableArrayGetter<T, R> getter) {
		return new ComparatorGetter<>(getter, nullsLast());
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
	 *
	 * @param elementComparator Comparator for array elements
	 * @return Comparator
	 * @param <R> Array component type
	 */
	static <R extends Comparable<? super R>> Comparator<R[]> compareElementsBy(Comparator<R> elementComparator) {
		return (a, b) -> compareLexicographically(a, b, elementComparator);
	}

	/**
	 * Compares two arrays lexicographically.
	 *
	 * @param a Left array
	 * @param b Right array
	 * @param <C> Comparable type with natural ordering
	 * @return Comparison result
	 */
	static <C extends Comparable<? super C>> int compareLexicographically(C[] a, C[] b) {
		return compareLexicographically(a, b, Comparator.naturalOrder());
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
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Objects.requireNonNull(comparator);

		if (a == b) {
			return 0;
		}

		int len = Math.min(a.length, b.length);
		for (int i = 0; i < len; ++i) {
			int result = comparator.compare(a[i], b[i]);
			if (result != 0) {
				return result;
			}
		}

		return a.length - b.length;
	}

}
