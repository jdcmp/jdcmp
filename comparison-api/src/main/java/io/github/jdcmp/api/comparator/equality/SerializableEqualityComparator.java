package io.github.jdcmp.api.comparator.equality;


import io.github.jdcmp.api.documentation.ThreadSafe;

import java.io.Serializable;

/**
 * A Serializable {@link EqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface SerializableEqualityComparator<T> extends EqualityComparator<T>, Serializable {

}
