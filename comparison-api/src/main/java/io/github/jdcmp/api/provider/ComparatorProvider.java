package io.github.jdcmp.api.provider;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;


/**
 * Implementations must provide a public no-args constructor.
 */
@ThreadSafe
public interface ComparatorProvider {

	/**
	 * Creates a new comparator.
	 *
	 * @param spec Spec
	 * @param <T>  Type whose instances can be compared
	 * @return Comparator instance
	 */
	<T> EqualityComparator<T> createEqualityComparator(EqualityComparatorSpec<T> spec);

	/**
	 * Creates a new comparator.
	 *
	 * @param spec Spec
	 * @param <T>  Type whose instances can be compared
	 * @return Comparator instance
	 */
	<T> SerializableEqualityComparator<T> createSerializableEqualityComparator(SerializableEqualityComparatorSpec<T> spec);

	/**
	 * Creates a new comparator.
	 *
	 * @param spec Spec
	 * @param <T>  Type whose instances can be compared
	 * @return Comparator instance
	 */
	<T> OrderingComparator<T> createOrderingComparator(OrderingComparatorSpec<T> spec);

	/**
	 * Creates a new comparator.
	 *
	 * @param spec Spec
	 * @param <T>  Type whose instances can be compared
	 * @return Comparator instance
	 */
	<T> SerializableOrderingComparator<T> createSerializableOrderingComparator(SerializableOrderingComparatorSpec<T> spec);

}
