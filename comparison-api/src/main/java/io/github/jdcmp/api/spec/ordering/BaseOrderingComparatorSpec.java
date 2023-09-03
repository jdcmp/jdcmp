package io.github.jdcmp.api.spec.ordering;

import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.spec.Spec;

import java.util.Optional;


/**
 * Base contract for information necessary to create an {@link OrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 * @param <G> Type of the getter
 */
public interface BaseOrderingComparatorSpec<T, G extends OrderingCriterion<? super T>> extends Spec<T, G> {

	/**
	 * Returns the null handling, i.e. what happens if either of the objects to compared is null.
	 *
	 * @return The null handling
	 */
	NullHandling getNullHandling();

	/**
	 * Returns the fallback mode, i.e. what happens if the user did not supply any criteria to compare objects.
	 *
	 * @return The fallback mode
	 */
	Optional<OrderingFallbackMode> getFallbackMode();

}
