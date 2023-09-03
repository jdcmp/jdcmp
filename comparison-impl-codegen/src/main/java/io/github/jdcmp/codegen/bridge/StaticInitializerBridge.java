package io.github.jdcmp.codegen.bridge;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import io.github.jdcmp.codegen.documentation.CalledFromGeneratedCode;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * <p>Serves as an information broker between class initialization and the static initializer of the generated classes. If constant pool
 * patching (defineAnonymousClass) and hidden classData (defineHiddenClassWithClassData) are both unavailable, this bridge is used
 * as a fallback.</p>
 *
 * <p>Despite being public (technical reasons), <strong>this class is not part of the public API</strong>, cannot be relied upon and
 * should not be used directly.</p>
 */
@ThreadSafe
public final class StaticInitializerBridge {

	private static final ThreadLocal<Registration<?>> THREAD_LOCAL = new ThreadLocal<>();

	/**
	 * Registers the given spec as dependencies for the given comparator class, then runs the given action.
	 *
	 * @param comparatorClass The generated comparator class
	 * @param spec Dependencies
	 * @param callable User action
	 * @return The user action's result
	 * @param <T> Type of the user action's result
	 */
	public static <T> T run(Class<?> comparatorClass, Spec<?, ?> spec, Callable<? extends T> callable) {
		THREAD_LOCAL.set(new Registration<>(comparatorClass, spec));

		try {
			return callable.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			THREAD_LOCAL.remove();
		}
	}

	/**
	 * Resolves dependencies for {@link EqualityComparator}.
	 *
	 * @param caller Lookup of caller
	 * @return The resolved dependencies
	 */
	@CalledFromGeneratedCode
	public static EqualityComparatorSpec<?> equality(Lookup caller) {
		return resolve(caller, EqualityComparatorSpec.class);
	}

	/**
	 * Resolves dependencies for {@link SerializableEqualityComparator}.
	 *
	 * @param caller Lookup of caller
	 * @return The resolved dependencies
	 */
	@CalledFromGeneratedCode
	public static SerializableEqualityComparatorSpec<?> equalitySerializable(Lookup caller) {
		return resolve(caller, SerializableEqualityComparatorSpec.class);
	}

	/**
	 * Resolves dependencies for {@link OrderingComparator}.
	 *
	 * @param caller Lookup of caller
	 * @return The resolved dependencies
	 */
	@CalledFromGeneratedCode
	public static OrderingComparatorSpec<?> ordering(Lookup caller) {
		return resolve(caller, OrderingComparatorSpec.class);
	}

	/**
	 * Resolves dependencies for {@link SerializableOrderingComparator}.
	 *
	 * @param caller Lookup of caller
	 * @return The resolved dependencies
	 */
	@CalledFromGeneratedCode
	public static SerializableOrderingComparatorSpec<?> orderingSerializable(Lookup caller) {
		return resolve(caller, SerializableOrderingComparatorSpec.class);
	}

	private static <T> T resolve(Lookup caller, Class<T> expectedSpecClass) {
		try {
			Registration<?> registration = THREAD_LOCAL.get();

			if (registration == null) {
				throw new IllegalStateException("No spec is registered");
			} else if (registration.comparatorClass != caller.lookupClass()) {
				throw new IllegalArgumentException("Caller provided an unexpected class: " + caller.lookupClass());
			}

			return expectedSpecClass.cast(registration.spec);
		} finally {
			THREAD_LOCAL.remove();
		}
	}

	private static final class Registration<T> {

		private final Class<?> comparatorClass;

		private final T spec;

		Registration(Class<?> comparatorClass, T spec) {
			this.comparatorClass = Objects.requireNonNull(comparatorClass);
			this.spec = Objects.requireNonNull(spec);
		}

	}

	private StaticInitializerBridge() {
		throw new AssertionError("No instances");
	}

}
