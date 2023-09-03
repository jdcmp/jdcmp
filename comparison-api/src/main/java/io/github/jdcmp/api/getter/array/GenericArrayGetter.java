package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.object.GenericGetter;

/**
 * Getter for generic arrays.
 *
 * @param <T> Type of the object containing the generic array
 */
@FunctionalInterface
public interface GenericArrayGetter<T, R> extends GenericGetter<T, R[]> {

}
