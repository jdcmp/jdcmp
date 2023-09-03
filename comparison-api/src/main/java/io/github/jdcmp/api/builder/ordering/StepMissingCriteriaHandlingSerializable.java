package io.github.jdcmp.api.builder.ordering;

/**
 * Step in the build process that decides what should happen if the user tries to create a comparator without any getters.
 */
public interface StepMissingCriteriaHandlingSerializable {

	/**
	 * See {@link StepMissingCriteriaHandling#requireAtLeastOneGetter(Class)}.
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> StepMandatorySerializableGetter<T> requireAtLeastOneGetter(Class<T> classToCompare);

	/**
	 * See {@link StepMissingCriteriaHandling#fallbackToNaturalOrdering(Class)}.
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T extends Comparable<? super T>> SerializableOrderingComparatorBuilder<T> fallbackToNaturalOrdering(Class<T> classToCompare);

	/**
	 * See {@link StepMissingCriteriaHandling#fallbackToIdentity(Class)}.
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> SerializableOrderingComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare);

}
