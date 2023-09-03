package io.github.jdcmp.api;


import io.github.jdcmp.api.builder.equality.StepSerializationEquality;
import io.github.jdcmp.api.builder.ordering.StepSerializationOrdering;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;

/**
 * <p>This static factory returns fluent builders to construct comparators.</p>
 *
 * <hr>
 * <p>
 * There are two types of comparators:
 * <dl>
 * 	<dt>{@link EqualityComparator}</dt>
 * 		<dd>
 * 			Provides implementations for the following methods:
 * 			<ul>
 *             	<li>{@link Object#hashCode()}</li>
 *             	<li>{@link Object#equals(Object)}</li>
 *         	</ul>
 * 		</dd>
 * 		<dt>{@link OrderingComparator}</dt>
 * 		<dd>
 * 			Provides implementations for the following methods:
 *         	<ul>
 *         		<li>{@link Object#hashCode()}</li>
 *         		<li>{@link Object#equals(Object)}</li>
 *				<li>{@link Comparable#compareTo(Object)}</li>
 *         </ul>
 *     </dd>
 * </dl>
 */
@ThreadSafe
public final class Comparators {

	/**
	 * Returns a fluent builder to construct an {@link EqualityComparator}.
	 * The returned builder is not guaranteed to be thread-safe.
	 *
	 * @return A new builder instance
	 */
	public static StepSerializationEquality equality() {
		return EqualityComparators.builder();
	}

	/**
	 * Returns a fluent builder to construct an {@link OrderingComparator}.
	 * The returned builder is not guaranteed to be thread-safe.
	 *
	 * @return A new builder instance
	 */
	public static StepSerializationOrdering ordering() {
		return OrderingComparators.builder();
	}

	private Comparators() {
		throw new AssertionError("No instances");
	}

}
