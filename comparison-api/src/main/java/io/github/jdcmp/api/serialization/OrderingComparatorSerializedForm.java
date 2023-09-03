package io.github.jdcmp.api.serialization;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;

import java.io.Serializable;
import java.util.Optional;

/**
 * Serialized form of {@link OrderingComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface OrderingComparatorSerializedForm<T> extends Serializable {

	/**
	 * Returns the type whose instances can be compared.
	 *
	 * @return Class representing the type
	 */
	Class<T> getClassToCompare();

	/**
	 * Returns the hashCode customization.
	 *
	 * @return Customization for hashCode
	 */
	HashParameters getHashParameters();

	/**
	 * Indicates whether strict type checking is enabled.
	 *
	 * @return true if strict types are enabled; false if strict types are disabled
	 */
	boolean getStrictTypes();

	/**
	 * Returns the getters.
	 *
	 * @return Criteria used in comparisons
	 */
	SerializableOrderingCriterion<T>[] getGetters();

	/**
	 * Returns the null handling mode.
	 *
	 * @return Null handling mode
	 */
	NullHandling getNullHandling();

	/**
	 * Returns the fallback mode for missing criteria.
	 *
	 * @return Fallback mode for missing criteria
	 */
	Optional<OrderingFallbackMode> getFallbackMode();

}
