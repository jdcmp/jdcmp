package io.github.jdcmp.api.spec.equality;

import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.serialization.EqualityComparatorSerializedForm;

/**
 * Spec for {@link SerializableEqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface SerializableEqualityComparatorSpec<T> extends BaseEqualityComparatorSpec<T, SerializableEqualityCriterion<T>> {

	/**
	 * Returns the spec's serialized form.
	 *
	 * @return A serialized form
	 */
	EqualityComparatorSerializedForm<T> toSerializedForm();

}
