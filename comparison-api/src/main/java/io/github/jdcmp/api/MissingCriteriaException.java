package io.github.jdcmp.api;

/**
 * Indicates that criteria is missing when building a comparator. To determine equality aside from comparing identities,
 * at least one criterion is required.
 */
public final class MissingCriteriaException extends IllegalArgumentException {

	/**
	 * Constructs an exception indicating missing criteria and no fallback.
	 *
	 * @return A new exception instance
	 */
	public static MissingCriteriaException of() {
		String message = "At least one criterion is required for comparisons.";
		message += " Provide at least one getter, or use a fallback such as identity comparison.";

		return new MissingCriteriaException(message);
	}

	private MissingCriteriaException(String message) {
		super(message);
	}

}
