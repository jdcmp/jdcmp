package io.github.jdcmp.api.builder.ordering;

import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * Helper interface for shorter return types.
 *
 * @param <T> Type whose instances can be compared
 */
public interface StepMandatorySerializableGetter<T> extends
		SharedGetter<T, SerializableOrderingComparatorBuilder<T>, SerializableOrderingCriterion<? super T>>,
		SharedGetters<T, SerializableOrderingComparatorBuilder<T>, SerializableOrderingCriterion<? super T>> {

}
