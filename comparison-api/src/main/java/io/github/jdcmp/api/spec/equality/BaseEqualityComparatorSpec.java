package io.github.jdcmp.api.spec.equality;

import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.getter.EqualityCriterion;
import io.github.jdcmp.api.spec.Spec;

import java.util.Optional;

/**
 * Base contract for information necessary to create an {@link EqualityComparator}.
 *
 * @param <T> Type whose instances can be compared
 * @param <G> Type of the getter
 */
public interface BaseEqualityComparatorSpec<T, G extends EqualityCriterion<? super T>> extends Spec<T, G> {

	/**
	 * Returns the fallback mode, i.e. what happens if the user did not supply any criteria to compare objects.
	 * A fallback mode is only available if the spec contains no getters.
	 *
	 * @return An Optional containing the fallback mode; or an empty Optional
	 */
	Optional<EqualityFallbackMode> getFallbackMode();

}
