package io.github.jdcmp.api.builder.equality;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;

/**
 * Step in the build process that decides what should happen if the user tries to create a comparator without any getters.
 */
public interface StepMissingCriteriaHandling {

	/**
	 * <p>
	 * Configures the {@link EqualityComparator} in such a way that, in the absence of configured getters,
	 * an exception is thrown during the construction process.
	 * </p>
	 *
	 * <hr>
	 *
	 * <strong>Protection from accidental changes</strong>
	 * <br>
	 * Sometimes programmers accidentally delete lines (i.e. IDE freezes briefly, then double-pressing a hotkey). In the example below,
	 * the <code>build()</code> method throws an exception if the getter (<code>Employee::getBirthday</code>) has been forgotten.
	 * <pre>
	 * {@code
	 * private static final OrderingComparator<Employee> COMPARATOR = Comparators.ordering()
	 * 		.nonSerializable()
	 * 		.requireAtLeastOneGetter(Employee.class)
	 * 		.use(Employee::getBirthday) // This line cannot be deleted without causing compilation errors
	 * 		.build();
	 * }
	 * </pre>
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> StepMandatoryGetter<T> requireAtLeastOneGetter(Class<T> classToCompare);

	/**
	 * Configures the {@link EqualityComparator} in such a way that, in the absence of configured getters, an identity check (<code>x == y</code>)
	 * is used to determine equality. That is, the method {@link EqualityComparator#areEqual(Object, Object)} returns true only if both arguments
	 * are null, or refer to the same object instance.
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> EqualityComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare);

}
