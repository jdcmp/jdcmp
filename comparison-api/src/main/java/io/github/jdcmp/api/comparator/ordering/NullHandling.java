package io.github.jdcmp.api.comparator.ordering;

/**
 * Contains strategies for handling null references.
 */
public enum NullHandling {

	/**
	 * A strategy that throws an exception if null is encountered.
	 */
	THROW {
		@Override
		public int getComparisonResultIfLeftSideIsNull() {
			throw new UnsupportedOperationException("Null is forbidden.");
		}

		@Override
		public <T> T map(NullHandlingMapper<T> mapper) {
			return mapper.onThrow();
		}
	},
	/**
	 * A strategy that orders nulls before other values.
	 */
	NULLS_FIRST {
		@Override
		public int getComparisonResultIfLeftSideIsNull() {
			return -1;
		}

		@Override
		public <T> T map(NullHandlingMapper<T> mapper) {
			return mapper.onNullsFirst();
		}
	},
	/**
	 * A strategy that orders nulls after other values.
	 */
	NULLS_LAST {
		@Override
		public int getComparisonResultIfLeftSideIsNull() {
			return 1;
		}

		@Override
		public <T> T map(NullHandlingMapper<T> mapper) {
			return mapper.onNullsLast();
		}
	};

	/**
	 * Returns the result of the comparison if only the left side is null.	 *
	 *
	 * @return An integer, typically -1 or +1
	 */
	abstract public int getComparisonResultIfLeftSideIsNull();

	/**
	 * Maps the enum constant using the given mapper. See {@link NullHandlingMapper}.
	 *
	 * @param mapper The mapper
	 * @param <T>    Type of the return value
	 * @return A user-supplied return value
	 */
	abstract public <T> T map(NullHandlingMapper<T> mapper);

	/**
	 * <p>Maps a null-handling mode to a user-supplied value. For every enum constant, there is one handler method present in
	 * this mapper.</p>
	 *
	 * <p>If any enum constants are added in the future, using this mapper will force implementors to handle the new
	 * "on"-method. This reduces the risk of missing cases in <code>if</code> or <code>switch</code> statements.</p>
	 *
	 * @param <T> Type of the return value
	 */
	public interface NullHandlingMapper<T> {

		/**
		 * Invoked when {@link NullHandling#THROW} is used.
		 *
		 * @return User-supplied return value
		 */
		T onThrow();

		/**
		 * Invoked when {@link NullHandling#NULLS_FIRST} is used.
		 *
		 * @return User-supplied return value
		 */
		T onNullsFirst();

		/**
		 * Invoked when {@link NullHandling#NULLS_LAST} is used.
		 *
		 * @return User-supplied return value
		 */
		T onNullsLast();

	}

}
