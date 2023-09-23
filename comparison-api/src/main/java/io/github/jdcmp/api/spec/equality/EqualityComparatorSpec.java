package io.github.jdcmp.api.spec.equality;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.getter.EqualityCriterion;

/**
 * Spec for {@link EqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface EqualityComparatorSpec<T> extends BaseEqualityComparatorSpec<T, EqualityCriterion<? super T>> {

}
