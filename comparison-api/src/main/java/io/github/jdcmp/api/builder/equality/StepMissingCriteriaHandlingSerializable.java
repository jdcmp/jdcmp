package io.github.jdcmp.api.builder.equality;

/**
 * Step in the build process that decides what should happen if the user tries to create a comparator without any getters.
 */
public interface StepMissingCriteriaHandlingSerializable {

	/**
	 * See {@link StepMissingCriteriaHandling#requireAtLeastOneGetter(Class)}.
	 *
	 * @param classToCompare The class whose instances can be compared
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> StepMandatorySerializableGetter<T> requireAtLeastOneGetter(Class<T> classToCompare);

	/**
	 * See {@link StepMissingCriteriaHandling#fallbackToIdentity(Class)}.
	 *
	 * @param classToCompare The class whose instances can be compared
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> SerializableEqualityComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare);

}
