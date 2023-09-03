package io.github.jdcmp.api.builder.ordering;

import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;

import java.io.Serializable;

/**
 * Step in the build process that decides whether the comparator is {@link Serializable} or non-serializable.
 */
public interface StepSerializationOrdering {

	/**
	 * Continue building a non-serializable comparator.
	 *
	 * @return A builder that produces {@link OrderingComparator}
	 */
	StepMissingCriteriaHandling nonSerializable();

	/**
	 * Continue building a serializable comparator.
	 *
	 * @return A builder that produces {@link SerializableOrderingComparator}
	 */
	StepMissingCriteriaHandlingSerializable serializable();

}
