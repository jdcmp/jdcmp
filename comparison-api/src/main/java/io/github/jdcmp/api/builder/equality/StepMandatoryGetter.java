package io.github.jdcmp.api.builder.equality;

import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.getter.EqualityCriterion;

/**
 * Helper interface for shorter return types.
 *
 * @param <T> Type whose instances can be compared
 */
public interface StepMandatoryGetter<T> extends
		SharedGetter<T, EqualityComparatorBuilder<T>, EqualityCriterion<? super T>>,
		SharedGetters<T, EqualityComparatorBuilder<T>, EqualityCriterion<? super T>> {

}
