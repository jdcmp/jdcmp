package io.github.jdcmp.api.getter;


import io.github.jdcmp.api.documentation.ThreadSafe;

import java.io.Serializable;

/**
 * Serializable {@link EqualityCriterion}.
 *
 * @param <T> Type whose instances can be compared
 */
@ThreadSafe
public interface SerializableEqualityCriterion<T> extends EqualityCriterion<T>, Serializable {

}
