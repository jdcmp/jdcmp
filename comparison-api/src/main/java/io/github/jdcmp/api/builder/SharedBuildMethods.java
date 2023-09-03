package io.github.jdcmp.api.builder;

import io.github.jdcmp.api.builder.equality.EqualityComparatorBuilder;
import io.github.jdcmp.api.builder.equality.SerializableEqualityComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.OrderingComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.SerializableOrderingComparatorBuilder;
import io.github.jdcmp.api.provider.ComparatorProvider;

import java.lang.invoke.MethodHandles;

/**
 * Final build step shared by {@link EqualityComparatorBuilder}, {@link SerializableEqualityComparatorBuilder},
 * {@link OrderingComparatorBuilder} and {@link SerializableOrderingComparatorBuilder}.
 *
 * @param <C> Type of the comparator
 */
public interface SharedBuildMethods<C> {

	/**
	 * Creates the comparator using a default provider.
	 *
	 * @return A new comparator instance
	 */
	C build();

	/**
	 * Creates the comparator using the given provider.
	 *
	 * @param comparatorProvider A provider
	 * @return A new comparator instance
	 */
	C build(ComparatorProvider comparatorProvider);

	/**
	 * Creates the comparator using the given provider and lookup.
	 *
	 * @param comparatorProvider A provider
	 * @param lookup A lookup
	 * @return A new comparator instance
	 */
	C build(ComparatorProvider comparatorProvider, MethodHandles.Lookup lookup);

}
