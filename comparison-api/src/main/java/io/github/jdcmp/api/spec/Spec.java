package io.github.jdcmp.api.spec;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.getter.EqualityCriterion;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Base type for comparator specifications.
 *
 * @param <T> Type whose instances can be compared
 * @param <G> Getter type
 */
public interface Spec<T, G extends EqualityCriterion<? super T>> {

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
	 * Returns the getters.
	 *
	 * @return Criteria used in comparisons
	 */
	Collection<? extends G> getGetters();

	/**
	 * Indicates whether serialization is enabled.
	 *
	 * @return true if serialization is enabled; false if serialization is disabled
	 */
	boolean isSerializable();

	/**
	 * Indicates whether strict type checking is enabled.
	 *
	 * @return true if strict types are enabled; false if strict types are disabled
	 */
	boolean useStrictTypes();

	/**
	 * Returns the lookup access context.
	 *
	 * @return An optional containing the lookup; or an empty optional
	 */
	Optional<Lookup> getLookup();

	/**
	 * Convenience method that returns the getters as a list.
	 *
	 * @return The getters as a list
	 */
	@SuppressWarnings("unused") // Called from generated code
	default List<? extends G> getGettersAsList() {
		return Collections.unmodifiableList(new ArrayList<>(getGetters()));
	}

	/**
	 * Returns the number of getters, i.e. how many criteria are used in comparisons.
	 *
	 * @return The number of getters
	 */
	default int getGetterCount() {
		return getGetters().size();
	}

	/**
	 * Indicates whether the spec is missing criteria in comparisons.
	 *
	 * @return true if no criteria is present; false otherwise
	 */
	default boolean hasNoGetters() {
		return getGetters().isEmpty();
	}

}
