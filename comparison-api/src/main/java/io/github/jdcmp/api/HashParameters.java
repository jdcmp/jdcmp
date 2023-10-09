package io.github.jdcmp.api;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.documentation.Immutable;

/**
 * Encapsulates parameters for the {@link EqualityComparator#hash(Object)} method.
 */
@Immutable
public final class HashParameters {

	private static final HashParameters JDK = new HashParameters(1, 31);

	private static final HashParameters DEFAULT = new HashParameters(17, 37);

	private final int initialValue;

	private final int multiplier;

	/**
	 * Returns an instance of {@link HashParameters}.
	 *
	 * @param initialValue The initial hash value
	 * @param multiplier   The hash multiplier
	 * @return An instance of HashParameters wrapping the given values
	 */
	public static HashParameters of(int initialValue, int multiplier) {
		if (initialValue == 1 && multiplier == 31) {
			return JDK;
		} else if (initialValue == 17 && multiplier == 37) {
			return DEFAULT;
		}

		return new HashParameters(initialValue, multiplier);
	}

	private HashParameters(int initialValue, int multiplier) {
		this.initialValue = initialValue;
		this.multiplier = multiplier;
	}

	/**
	 * Returns the initial value for hashCode calculation.
	 *
	 * @return The initial value
	 */
	public int initialValue() {
		return initialValue;
	}

	/**
	 * Returns the multiplier for hashCode calculation.
	 *
	 * @return The multiplier
	 */
	public int multiplier() {
		return multiplier;
	}

	@Override
	public int hashCode() {
		return 37 * initialValue + multiplier;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof HashParameters) {
			HashParameters o = (HashParameters) obj;
			return this.initialValue == o.initialValue && this.multiplier == o.multiplier;
		}

		return false;
	}

	@Override
	public String toString() {
		return "HashParameters[initialValue=" + initialValue + ", multiplier=" + multiplier + "]";
	}

}
