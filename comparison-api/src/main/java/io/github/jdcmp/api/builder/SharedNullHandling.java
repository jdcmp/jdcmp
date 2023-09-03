package io.github.jdcmp.api.builder;

import io.github.jdcmp.api.builder.ordering.OrderingComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.SerializableOrderingComparatorBuilder;
import io.github.jdcmp.api.comparator.ordering.NullHandling;

/**
 * Build step for getter configuration, shared by {@link OrderingComparatorBuilder} and {@link SerializableOrderingComparatorBuilder}.
 *
 * @param <B> Type of the builder
 */
public interface SharedNullHandling<B> {

	/**
	 * Configures how nulls are handled.
	 *
	 * @param nullHandling Null handling mode
	 * @return The fluent builder instance
	 */
	B nullHandling(NullHandling nullHandling);

	/**
	 * Configures that nulls are ordered before non-nulls.
	 *
	 * @return The fluent builder instance
	 */
	default B nullsFirst() {
		return nullHandling(NullHandling.NULLS_FIRST);
	}

	/**
	 * Configures that nulls are ordered after non-nulls.
	 *
	 * @return The fluent builder instance
	 */
	default B nullsLast() {
		return nullHandling(NullHandling.NULLS_LAST);
	}

}
