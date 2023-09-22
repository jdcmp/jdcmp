package io.github.jdcmp.api.spec.ordering;

import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.serialization.OrderingComparatorSerializedForm;

/**
 * Spec for {@link SerializableOrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface SerializableOrderingComparatorSpec<T> extends BaseOrderingComparatorSpec<T, SerializableOrderingCriterion<? super T>> {

	/**
	 * Returns the spec's serialized form.
	 *
	 * @return A serialized form
	 */
	OrderingComparatorSerializedForm<T> toSerializedForm();

}
