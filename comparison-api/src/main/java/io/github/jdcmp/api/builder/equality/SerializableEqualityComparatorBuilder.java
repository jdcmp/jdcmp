package io.github.jdcmp.api.builder.equality;

import io.github.jdcmp.api.builder.SharedBuildMethods;
import io.github.jdcmp.api.builder.SharedCustomization;
import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;

/**
 * A builder that produces {@link SerializableEqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface SerializableEqualityComparatorBuilder<T> extends
		SharedCustomization<SerializableEqualityComparatorBuilder<T>>,
		SharedGetters<T, SerializableEqualityComparatorBuilder<T>, SerializableEqualityCriterion<? super T>>,
		SharedGetter<T, SerializableEqualityComparatorBuilder<T>, SerializableEqualityCriterion<? super T>>,
		SharedBuildMethods<SerializableEqualityComparator<T>> {

}
