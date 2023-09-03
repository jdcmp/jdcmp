package io.github.jdcmp.api.getter;


import io.github.jdcmp.api.documentation.ThreadSafe;

import java.util.Comparator;

/**
 * Serializable {@link OrderingCriterion}.
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface SerializableOrderingCriterion<T> extends OrderingCriterion<T>, SerializableEqualityCriterion<T>, Comparator<T> {

}
