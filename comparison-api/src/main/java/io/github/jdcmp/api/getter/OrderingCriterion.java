package io.github.jdcmp.api.getter;


import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;

import java.util.Comparator;

/**
 * A single criterion, such as a field inside a class, used to determine equality as part of an {@link OrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface OrderingCriterion<T> extends EqualityCriterion<T>, Comparator<T> {

}
