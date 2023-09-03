package io.github.jdcmp.api.builder.equality;

import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;

/**
 * Helper interface for shorter return types.
 *
 * @param <T> Type whose instances can be compared
 */
public interface StepMandatorySerializableGetter<T> extends
		SharedGetter<T, SerializableEqualityComparatorBuilder<T>, SerializableEqualityCriterion<? super T>>,
		SharedGetters<T, SerializableEqualityComparatorBuilder<T>, SerializableEqualityCriterion<? super T>> {

}
