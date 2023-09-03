package io.github.jdcmp.api.builder.ordering;

/**
 * The fallback is used if no criteria (getters) are configured.
 */
public enum OrderingFallbackMode {

	/**
	 * Falls back to identity comparison (e.g. <code>x == y</code>).
	 */
	IDENTITY {
		@Override
		public <T> T map(FallbackMapper<T> mapper) {
			return mapper.onIdentity();
		}
	},
	/**
	 * Falls back to natural ordering. May cause infinite recursion if the comparator is used to implement
	 * {@link Comparable#compareTo(Object)}.
	 */
	NATURAL {
		@Override
		public <T> T map(FallbackMapper<T> mapper) {
			return mapper.onNatural();
		}
	};

	/**
	 * Maps the enum constant using the given mapper. See {@link FallbackMapper}.
	 *
	 * @param mapper The mapper
	 * @return A user-supplied return value
	 * @param <T> Type of the return value
	 */
	abstract public <T> T map(FallbackMapper<T> mapper);

	/**
	 * <p>Maps a fallback mode to a user-supplied value. For every enum constant, there is one handler method present in
	 * this mapper.</p>
	 *
	 * <p>If any enum constants are added in the future, using this mapper will force implementors to handle the new
	 * "on"-method. This reduces the risk of missing cases in <code>if</code> or <code>switch</code> statements.</p>
	 *
	 * @param <T> Type of the return value
	 */
	public interface FallbackMapper<T> {

		/**
		 * Invoked when {@link OrderingFallbackMode#IDENTITY} is used.
		 *
		 * @return User-supplied return value
		 */
		T onIdentity();

		/**
		 * Invoked when {@link OrderingFallbackMode#NATURAL} is used.
		 *
		 * @return User-supplied return value
		 */
		T onNatural();

	}

}
