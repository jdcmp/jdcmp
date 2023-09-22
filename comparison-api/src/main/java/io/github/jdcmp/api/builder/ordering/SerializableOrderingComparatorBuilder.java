package io.github.jdcmp.api.builder.ordering;

import io.github.jdcmp.api.builder.SharedBuildMethods;
import io.github.jdcmp.api.builder.SharedCustomization;
import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.builder.SharedNullHandling;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

/**
 * A builder that produces {@link SerializableOrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface SerializableOrderingComparatorBuilder<T>
		extends
		SharedGetters<T, SerializableOrderingComparatorBuilder<T>, SerializableOrderingCriterion<? super T>>,
		SharedGetter<T, SerializableOrderingComparatorBuilder<T>, SerializableOrderingCriterion<? super T>>,
		SharedCustomization<SerializableOrderingComparatorBuilder<T>>,
		SharedNullHandling<SerializableOrderingComparatorBuilder<T>>,
		SharedBuildMethods<SerializableOrderingComparator<T>> {

}
