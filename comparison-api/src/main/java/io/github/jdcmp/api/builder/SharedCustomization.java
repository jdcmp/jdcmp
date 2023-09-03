package io.github.jdcmp.api.builder;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.equality.EqualityComparatorBuilder;
import io.github.jdcmp.api.builder.equality.SerializableEqualityComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.OrderingComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.SerializableOrderingComparatorBuilder;

/**
 * Customization shared by {@link EqualityComparatorBuilder}, {@link SerializableEqualityComparatorBuilder},
 * {@link OrderingComparatorBuilder} and {@link SerializableOrderingComparatorBuilder}.
 *
 * @param <B> Type of the builder
 */
public interface SharedCustomization<B> {

	/**
	 * Allows customizing the hashCode method.
	 *
	 * @param hashParameters Customization for hashCode
	 * @return The fluent builder instance
	 */
	B hashParameters(HashParameters hashParameters);

	/**
	 * Configures whether the comparator will do a type check on the supplied parameters.
	 *
	 * @param strictTypes true to enable strict type checks; false to disable strict type checks
	 * @return The fluent builder instance
	 */
	B strictTypes(boolean strictTypes);

}
