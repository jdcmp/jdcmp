package io.github.jdcmp.api.comparator.ordering;


import io.github.jdcmp.api.documentation.ThreadSafe;

import java.io.Serializable;

/**
 * Serializable {@link OrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface SerializableOrderingComparator<T> extends OrderingComparator<T>, Serializable {

}
