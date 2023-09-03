package io.github.jdcmp.api.builder.equality;

import io.github.jdcmp.api.builder.SharedBuildMethods;
import io.github.jdcmp.api.builder.SharedCustomization;
import io.github.jdcmp.api.builder.SharedGetter;
import io.github.jdcmp.api.builder.SharedGetters;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.getter.EqualityCriterion;

/**
 * A builder that produces {@link EqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface EqualityComparatorBuilder<T>
		extends
		SharedGetters<T, EqualityComparatorBuilder<T>, EqualityCriterion<? super T>>,
		SharedGetter<T, EqualityComparatorBuilder<T>, EqualityCriterion<? super T>>,
		SharedCustomization<EqualityComparatorBuilder<T>>,
		SharedBuildMethods<EqualityComparator<T>> {

}
