package io.github.jdcmp.api.builder;

import io.github.jdcmp.api.builder.equality.EqualityComparatorBuilder;
import io.github.jdcmp.api.builder.equality.SerializableEqualityComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.OrderingComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.SerializableOrderingComparatorBuilder;

/**
 * Build step for getter configuration, shared by {@link EqualityComparatorBuilder}, {@link SerializableEqualityComparatorBuilder},
 * {@link OrderingComparatorBuilder} and {@link SerializableOrderingComparatorBuilder}.
 *
 * @param <T> Type whose instances can be compared
 * @param <B> Type of the builder
 * @param <G> Type of the getter
 */
public interface SharedGetter<T, B, G> {

	/**
	 * Adds a criterion.
	 *
	 * @param getter A criterion to be used in the comparison
	 * @return The fluent builder instance
	 */
	B use(G getter);

}
