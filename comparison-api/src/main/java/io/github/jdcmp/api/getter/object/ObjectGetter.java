package io.github.jdcmp.api.getter.object;

/**
 * Getter for {@link Object}.
 *
 * @param <T> Type of the object containing the {@link Object}
 */
@FunctionalInterface
public interface ObjectGetter<T> extends GenericGetter<T, Object> {

	/**
	 * <p>Type inference helper method to create inline getters from lambda expressions.</p>
	 *
	 * <p>Example: <code>ObjectGetter.of(MyClass::getSomeObject)</code></p>
	 *
	 * @param getter The getter
	 * @return The getter
	 * @param <T> Type whose instances can be compared using the getter
	 */
	static <T> ObjectGetter<T> of(ObjectGetter<T> getter) {
		return getter;
	}

}
