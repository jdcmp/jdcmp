package io.github.jdcmp.api.spec.ordering;

import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * Spec for {@link OrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface OrderingComparatorSpec<T> extends BaseOrderingComparatorSpec<T, OrderingCriterion<T>> {

}
