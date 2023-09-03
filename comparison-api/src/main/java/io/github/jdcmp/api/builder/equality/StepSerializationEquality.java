package io.github.jdcmp.api.builder.equality;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;

import java.io.Serializable;

/**
 * Step in the build process that decides whether the comparator is {@link Serializable} or non-serializable.
 */
public interface StepSerializationEquality {

	/**
	 * Continue building a non-serializable comparator for the given type.
	 *
	 * @return A builder that produces {@link EqualityComparator}
	 */
	StepMissingCriteriaHandling nonSerializable();

	/**
	 * Continue building a serializable comparator for the given type.
	 *
	 * @return A builder that produces {@link SerializableEqualityComparator}
	 */
	StepMissingCriteriaHandlingSerializable serializable();

}
