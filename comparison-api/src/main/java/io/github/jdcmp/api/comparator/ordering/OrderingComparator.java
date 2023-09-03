package io.github.jdcmp.api.comparator.ordering;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;

import java.util.Comparator;

/**
 * <p>A comparator that, in addition to determining {@link Comparable#compareTo(Object) ordering}, provides implementations of
 * {@link Object#hashCode()} and {@link Object#equals(Object)} in a consistent manner (i.e. based on the same criteria).</p>
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface OrderingComparator<T> extends EqualityComparator<T>, Comparator<T> {

}
