package io.github.jdcmp.api.builder.ordering;

import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.getter.OrderingCriterion;

/**
 * Helper interface for shorter return types.
 *
 * @param <T> Type whose instances can be compared
 */
public interface StepMandatoryGetter<T> extends
		SharedGetter<T, OrderingComparatorBuilder<T>, OrderingCriterion<? super T>>,
		SharedGetters<T, OrderingComparatorBuilder<T>, OrderingCriterion<? super T>> {

}
