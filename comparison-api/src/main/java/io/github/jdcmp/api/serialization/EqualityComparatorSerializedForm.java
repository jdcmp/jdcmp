package io.github.jdcmp.api.serialization;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;

import java.io.Serializable;
import java.util.Optional;

/**
 * Serialized form of {@link EqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 */
public interface EqualityComparatorSerializedForm<T> extends Serializable {

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
	SerializableEqualityCriterion<T>[] getGetters();

	/**
	 * Returns the fallback mode for missing criteria.
	 *
	 * @return Fallback mode for missing criteria
	 */
	Optional<EqualityFallbackMode> getFallbackMode();

}
