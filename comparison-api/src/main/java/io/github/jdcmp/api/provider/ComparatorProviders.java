package io.github.jdcmp.api.provider;

import io.github.jdcmp.api.documentation.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * <p>This static factory allows obtaining and managing instances of {@link ComparatorProvider}. Default providers can be configured via
 * {@link #setDefaultProvider(ComparatorProvider)} and {@link #setSerializationProvider(ComparatorProvider)}. In the absence of
 * explicit configuration, the {@link #load()} and {@link #loadForSerialization()} methods are used to obtain a default factory.</p>
 *
 * <hr>
 * <p>The current implementation attempts to reduce garbage collection overhead if no explicit provider is configured. This behavior
 * is an implementation detail that is not guaranteed and may change in the future. Users are encouraged to store a reference to a
 * provider somewhere (e.g. IoC-container) and pass it when building via {@link io.github.jdcmp.api.Comparators}.</p>
 */
@ThreadSafe
public final class ComparatorProviders {

	private static final Cache CACHE = new Cache();

	/**
	 * <p>
	 * Returns a default {@link ComparatorProvider}, or tries to find one using the {@link java.util.ServiceLoader}.
	 * </p>
	 *
	 * <p>
	 * There are no guarantees as to which {@link ComparatorProvider} will be selected if multiple implementations are present, unless
	 * {@link #setDefaultProvider(ComparatorProvider)} is used before calling this method.
	 * </p>
	 *
	 * @return A {@link ComparatorProvider}
	 * @throws UnsupportedOperationException If no implementation is available
	 */
	public static ComparatorProvider load() throws UnsupportedOperationException {
		return Cache.DEFAULT.load();
	}

	/**
	 * <p>
	 * Returns a default {@link ComparatorProvider} for serialization, or tries to find one using the {@link java.util.ServiceLoader}.
	 * </p>
	 *
	 * <p>
	 * There are no guarantees as to which {@link ComparatorProvider} will be selected if multiple implementations are present, unless
	 * {@link #setSerializationProvider(ComparatorProvider)} is used before calling this method.
	 * </p>
	 *
	 * @return A {@link ComparatorProvider}
	 * @throws UnsupportedOperationException If no implementation is available
	 */
	public static ComparatorProvider loadForSerialization() throws UnsupportedOperationException {
		return Cache.SERIALIZATION.load();
	}

	/**
	 * Loads the given {@link ComparatorProvider}. This method is useful if a specific implementation must be selected.
	 * Otherwise, the {@link #load()} method is more convenient.
	 *
	 * @param clazz The provider's class
	 * @return An instance of the given provider
	 * @param <T> Type of the provider
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ComparatorProvider> T load(Class<? extends T> clazz) {
		return (T) CACHE.get(clazz);
	}

	/**
	 * Sets a default {@link ComparatorProvider} that will be used instead of loading via {@link java.util.ServiceLoader}.
	 *
	 * @param provider A provider; or null to use a default
	 */
	public static void setDefaultProvider(@Nullable ComparatorProvider provider) {
		Cache.DEFAULT.setConfigured(provider);
	}

	/**
	 * Sets a default {@link ComparatorProvider} that will be used for serialization instead of loading via
	 * {@link java.util.ServiceLoader}.
	 *
	 * @param provider A provider; or null to use a default
	 */
	public static void setSerializationProvider(@Nullable ComparatorProvider provider) {
		Cache.SERIALIZATION.setConfigured(provider);
	}

	@ThreadSafe
	private static final class Cache extends ClassValue<ComparatorProvider> {

		private static final WeakCache DEFAULT = new WeakCache(Cache::loadFromServiceLoader);

		private static final WeakCache SERIALIZATION = new WeakCache(Cache::loadFromServiceLoader);

		@Override
		protected ComparatorProvider computeValue(Class<?> clazz) {
			if (!ComparatorProvider.class.isAssignableFrom(clazz)) {
				throw new IllegalArgumentException("Class must implement " + ComparatorProvider.class.getCanonicalName());
			}

			@SuppressWarnings("unchecked")
			Class<? extends ComparatorProvider> providerClass = (Class<? extends ComparatorProvider>) clazz;

			return loadSpecificImpl(providerClass);
		}

		private static ComparatorProvider loadSpecificImpl(Class<? extends ComparatorProvider> providerClass) {
			Constructor<? extends ComparatorProvider> constructor;

			try {
				constructor = providerClass.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
				String message = String.format("Implementation %s violates the contract of %s. The no-args constructor is missing.",
						providerClass.getCanonicalName(),
						ComparatorProvider.class.getCanonicalName());
				throw new IllegalArgumentException(message, e);
			}

			if (!Modifier.isPublic(constructor.getModifiers())) {
				String message = String.format("Implementation %s violates the contract of %s. The no-args constructor is not public.",
						providerClass.getCanonicalName(),
						ComparatorProvider.class.getCanonicalName());
				throw new IllegalArgumentException(message);
			}

			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Failed to instantiate the given implementation: " + providerClass.getCanonicalName(), e);
			}
		}

		private static ComparatorProvider loadFromServiceLoader() {
			for (ComparatorProvider provider : ServiceLoader.load(ComparatorProvider.class)) {
				return provider;
			}

			throw missingImplementationException();
		}

		private static UnsupportedOperationException missingImplementationException() throws UnsupportedOperationException {
			String className = ComparatorProvider.class.getCanonicalName();

			return new UnsupportedOperationException("Did not find an appropriate implementation of "
					+ className + " on the class path using " + ServiceLoader.class.getName() + ".");
		}

	}

	@ThreadSafe
	private static final class WeakCache {

		private final Supplier<? extends ComparatorProvider> factory;

		private volatile @Nullable WeakReference<ComparatorProvider> configured;

		private volatile @Nullable WeakReference<ComparatorProvider> cached;

		WeakCache(Supplier<? extends ComparatorProvider> factory) {
			this.factory = Objects.requireNonNull(factory);
		}

		void setConfigured(ComparatorProvider comparatorProvider) {
			this.configured = new WeakReference<>(comparatorProvider);
		}

		ComparatorProvider load() {
			ComparatorProvider comparatorProvider = firstNonNull(configured, cached);

			return comparatorProvider != null ? comparatorProvider : loadNewInstance();
		}

		private synchronized ComparatorProvider loadNewInstance() {
			ComparatorProvider comparatorProvider = unwrap(this.cached);

			if (comparatorProvider == null) {
				this.cached = new WeakReference<>(comparatorProvider = factory.get());
			}

			return comparatorProvider;
		}

		private static <T> @Nullable T firstNonNull(@Nullable WeakReference<T> primary, @Nullable WeakReference<T> alternative) {
			T provider = unwrap(primary);

			return provider != null ? provider : unwrap(alternative);
		}

		private static <T> @Nullable T unwrap(@Nullable WeakReference<T> ref) {
			return ref == null ? null : ref.get();
		}

	}

	private ComparatorProviders() {
		throw new AssertionError("No instances");
	}

}
