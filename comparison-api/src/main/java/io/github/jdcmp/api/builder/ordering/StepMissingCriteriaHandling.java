package io.github.jdcmp.api.builder.ordering;

import io.github.jdcmp.api.comparator.ordering.OrderingComparator;

import java.util.Comparator;

/**
 * Step in the build process that decides what should happen if the user tries to create a comparator without any getters.
 */
public interface StepMissingCriteriaHandling {

	/**
	 * <p>
	 * Configures the {@link OrderingComparator} in such a way that, in the absence of configured getters,
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
	 * <p>
	 * Configures the {@link OrderingComparator} in such a way that, in the absence of configured getters, the type's natural ordering is
	 * used for both {@link OrderingComparator#areEqual(Object, Object)} and {@link OrderingComparator#compare(Object, Object)}.
	 * </p>
	 *
	 * <hr>
	 *
	 * <strong>Example without getters</strong>
	 * <br>
	 * <p>
	 * Since there are no getters, the following example behaves like {@link Comparator#naturalOrder()}.
	 * <pre>
	 * {@code
	 * private static final OrderingComparator<String> COMPARATOR = Comparators.ordering()
	 * 		.nonSerializable()
	 * 		.fallbackToNaturalOrdering(String.class)
	 * 		.build();
	 * }
	 * </pre>
	 *
	 * <strong>Example with getters</strong>
	 * <br>
	 *
	 * <pre>
	 * {@code
	 * private static final OrderingComparator<Employee> COMPARATOR = Comparators.ordering()
	 * 		.nonSerializable()
	 * 		.fallbackToNaturalOrdering(Employee.class)
	 * 		.use(Employee::getBirthday)
	 * 		.use(Employee::getId)
	 * 		.build();
	 * }
	 * </pre>
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T extends Comparable<? super T>> OrderingComparatorBuilder<T> fallbackToNaturalOrdering(Class<T> classToCompare);

	/**
	 * Configures an identity fallback to be used in the absence of getters. This results in inconsistent behavior between
	 * {@link OrderingComparator#areEqual(Object, Object)} and {@link OrderingComparator#compare(Object, Object)}.
	 *
	 * <ul>
	 *     <li>An identity check is used to determine equality. That is, the method
	 *     {@link OrderingComparator#areEqual(Object, Object)} checks if both arguments refer to the same object,
	 *     or if both arguments are null.</li>
	 *     <li>The method {@link OrderingComparator#compare(Object, Object)} returns 0 regardless of input parameters,
	 *     unless <code>null</code> is passed and the comparator does not permit nullable references.</li>
	 * </ul>
	 *
	 * @param classToCompare The class whose instances the comparator will compare
	 * @param <T>            Type whose instances can be compared
	 * @return The next step in the build process
	 */
	<T> OrderingComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare);

}
