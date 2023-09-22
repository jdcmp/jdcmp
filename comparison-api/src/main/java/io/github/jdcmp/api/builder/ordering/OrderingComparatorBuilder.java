package io.github.jdcmp.api.builder.ordering;

import io.github.jdcmp.api.builder.SharedBuildMethods;
import io.github.jdcmp.api.builder.SharedCustomization;
import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.builder.SharedNullHandling;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * A builder that produces {@link OrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface OrderingComparatorBuilder<T>
		extends
		SharedGetters<T, OrderingComparatorBuilder<T>, OrderingCriterion<? super T>>,
		SharedGetter<T, OrderingComparatorBuilder<T>, OrderingCriterion<? super T>>,
		SharedCustomization<OrderingComparatorBuilder<T>>,
		SharedNullHandling<OrderingComparatorBuilder<T>>,
		SharedBuildMethods<OrderingComparator<T>> {

}
