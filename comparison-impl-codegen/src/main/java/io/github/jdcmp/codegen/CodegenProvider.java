package io.github.jdcmp.codegen;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.provider.ComparatorProvider;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import io.github.jdcmp.codegen.ClassDefiners.*;
import io.github.jdcmp.codegen.bridge.GeneratedClassHolder;
import io.github.jdcmp.codegen.contract.EventHandler;
import io.github.jdcmp.codegen.contract.LookupFactory;
import io.github.jdcmp.codegen.customization.AvailableClassDefiner;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode;
import io.github.jdcmp.codegen.customization.AvailableInstantiator;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode;
import io.github.jdcmp.codegen.documentation.CarefulRefactoring;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the ServiceProvider for <i>comparison-impl-codegen</i>.
 *
 */
@ThreadSafe
@CarefulRefactoring(reason = "Public API")
public final class CodegenProvider implements ComparatorProvider {

	private static final Logger LOGGER = Logger.getLogger(CodegenProvider.class.getName());

	private static final LookupFactory DEFAULT_LOOKUP_FACTORY
			= new ConstantLookupFactory(GeneratedClassHolder.lookup(MethodHandles.lookup()));

	private final Customization customization;

	private final ClassDefinerHolder classDefiners = new ClassDefinerHolder();

	// ********************************************************************************************
	// Instantiation
	// ********************************************************************************************

	/**
	 * Constructs a new instance using a default {@link LookupFactory}.
	 *
	 * @return The new provider instance
	 */
	public static CodegenProvider of() {
		return of(DEFAULT_LOOKUP_FACTORY);
	}

	/**
	 * Constructs a new instance using the given {@link Lookup}.
	 *
	 * @param lookup The lookup
	 * @return The new provider instance
	 */
	public static CodegenProvider of(Lookup lookup) {
		return of(new ConstantLookupFactory(lookup));
	}

	/**
	 * Constructs a new instance using the given {@link LookupFactory}.
	 *
	 * @param lookupFactory The factory that provides {@link Lookup Lookups}.
	 * @return The new provider instance
	 */
	public static CodegenProvider of(LookupFactory lookupFactory) {
		return new CodegenProvider(lookupFactory);
	}

	/**
	 * Constructs a new instance using a default {@link LookupFactory}. See {@link #of(LookupFactory)} and similar static factory
	 * methods.
	 */
	@CarefulRefactoring(reason = "Called via reflection or ServiceLoader, required for automatic instantiation")
	public CodegenProvider() {
		this(DEFAULT_LOOKUP_FACTORY);
	}

	private CodegenProvider(LookupFactory lookupFactory) {
		this.customization = new Customization(lookupFactory);
	}

	// ********************************************************************************************
	// Main API
	// ********************************************************************************************

	@Override
	public <T> EqualityComparator<T> createEqualityComparator(EqualityComparatorSpec<T> spec) {
		spec = Specs.equality(spec);
		ImplSpec implSpec = createImplSpec(spec);

		return EqualityComparators.create(spec, implSpec);
	}

	@Override
	public <T> SerializableEqualityComparator<T> createSerializableEqualityComparator(SerializableEqualityComparatorSpec<T> spec) {
		spec = Specs.equalitySerializable(spec);
		ImplSpec implSpec = createImplSpec(spec);

		return EqualityComparators.createSerializable(spec, implSpec);
	}

	@Override
	public <T> OrderingComparator<T> createOrderingComparator(OrderingComparatorSpec<T> spec) {
		spec = Specs.ordering(spec);
		ImplSpec implSpec = createImplSpec(spec);

		return OrderingComparators.create(spec, implSpec);
	}

	@Override
	public <T> SerializableOrderingComparator<T> createSerializableOrderingComparator(SerializableOrderingComparatorSpec<T> spec) {
		spec = Specs.orderingSerializable(spec);
		ImplSpec implSpec = createImplSpec(spec);

		return OrderingComparators.createSerializable(spec, implSpec);
	}

	// ********************************************************************************************
	// Publicly exposed configuration
	// ********************************************************************************************

	/**
	 * Sets an {@link EventHandler} that is called during the class generation process.
	 *
	 * @param eventHandler An event handler; or null to restore default settings
	 */
	public void setEventHandler(@Nullable EventHandler eventHandler) {
		this.customization.eventHandler = eventHandler;
	}

	/**
	 * Controls whether the implementation is compatible with Java's serialization mechanism.
	 *
	 * @param serializationMode The serialization mode; null to restore default settings
	 */
	@SuppressWarnings("unused")
	public void setSerializationMode(@Nullable AvailableSerializationMode serializationMode) {
		this.customization.serializationMode = serializationMode;
	}

	/**
	 * Configures strategies used for loading generated classes.
	 *
	 * @param classDefiners An array; null or empty to restore default settings
	 */
	public void setClassDefiners(@Nullable AvailableClassDefiner... classDefiners) {
		this.customization.classDefiners = Utils.immutableEnumSetOrNull(classDefiners);
	}

	/**
	 * Configures strategies used for creating instances of generated comparators.
	 *
	 * @param instantiators An array; null or empty to restore default settings
	 */
	public void setInstantiators(@Nullable AvailableInstantiator... instantiators) {
		this.customization.instantiators = Utils.immutableEnumSetOrNull(instantiators);
	}

	/**
	 * Configures a strategy for field initialization in generated classes.
	 *
	 * @param initializationMode An initialization mode; null to restore default settings
	 */
	public void setInitializationMode(@Nullable AvailableInitializationMode initializationMode) {
		this.customization.initializationMode = initializationMode;
	}

	/**
	 * Configures whether generic bridge methods will be generated.
	 *
	 * @param generateBridgeMethods True to generate bridge methods; null to restore default settings
	 */
	public void setGenerateBridgeMethods(@Nullable Boolean generateBridgeMethods) {
		this.customization.generateBridgeMethods = generateBridgeMethods;
	}

	// ********************************************************************************************
	// Other public methods
	// ********************************************************************************************

	@Override
	public String toString() {
		return "ComparatorProvider[customization=" + customization + "]";
	}

	// ********************************************************************************************
	// Internal implementation details
	// ********************************************************************************************

	private ImplSpec createImplSpec(Spec<?, ?> spec) {
		Customization customization = new Customization(this.customization);
		Lookup lookup = getLookup(spec, customization);
		EventHandler eventHandler = customization.getEventHandler();
		Collection<AvailableClassDefiner> availableClassDefiners = customization.getClassDefiners();
		ImplSpec.OptionalClassDefiners classDefiners = createOptionalClassDefiners(availableClassDefiners, spec);
		Collection<Instantiator> instantiators = Instantiators.create(customization.getInstantiators());
		ImplSpec.ClassGeneratorConfig classGeneratorConfig = customization.getClassGeneratorConfig();

		return new ImplSpec(lookup, eventHandler, classDefiners, instantiators, classGeneratorConfig);
	}

	private static Lookup getLookup(Spec<?, ?> spec, Customization customization) {
		Class<?> classToCompare = spec.getClassToCompare();
		Lookup lookup = spec.getLookup().orElseGet(() -> customization.getLookup(classToCompare));

		if (customization.generateBridgeMethods()) {
			try {
				Utils.verifyLookupCanAccess(lookup, classToCompare);
			} catch (IllegalAccessException e) {
				throw UnderprivilegedLookupException.of(lookup, classToCompare);
			}
		}

		return lookup;
	}

	private ImplSpec.OptionalClassDefiners createOptionalClassDefiners(Collection<AvailableClassDefiner> availableClassDefiners, Spec<?, ?> spec) {
		try {
			return new ImplSpec.OptionalClassDefiners(
					classDefiners.vmAnonymous(availableClassDefiners),
					classDefiners.lookupHiddenClassWithClassDataDefiner(availableClassDefiners),
					classDefiners.lookupHiddenClassDefiner(availableClassDefiners),
					classDefiners.lookupClassDefiner(availableClassDefiners),
					classDefiners.classLoaderClassDefiner(availableClassDefiners)
			);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("None of the following ClassDefiners is available: " + availableClassDefiners, e);
		}
	}

	@ThreadSafe
	private static final class Customization {

		private static final EventHandler DEFAULT_EVENT_HANDLER = NoopEventHandler.INSTANCE;

		private static final AvailableSerializationMode DEFAULT_SERIALIZATION_MODE = AvailableSerializationMode.COMPATIBLE;

		private static final Set<AvailableClassDefiner> DEFAULT_CLASS_DEFINERS = Utils.immutableEnumSet(AvailableClassDefiner.class);

		private static final Set<AvailableInstantiator> DEFAULT_INSTANTIATORS = Utils.immutableEnumSet(AvailableInstantiator.class);

		private static final AvailableInitializationMode DEFAULT_INITIALIZATION_MODE = AvailableInitializationMode.STATIC_INITIALIZER;

		public static final Boolean DEFAULT_GENERATE_BRIDGE_METHODS = Boolean.FALSE;

		private final LookupFactory lookupFactory;

		private volatile @Nullable EventHandler eventHandler;

		private volatile @Nullable AvailableSerializationMode serializationMode;

		private volatile @Nullable Set<AvailableClassDefiner> classDefiners;

		private volatile @Nullable Set<AvailableInstantiator> instantiators;

		private volatile @Nullable AvailableInitializationMode initializationMode;

		private volatile @Nullable Boolean generateBridgeMethods;

		Customization(LookupFactory lookupFactory) {
			this.lookupFactory = Objects.requireNonNull(lookupFactory);
		}

		Customization(Customization customization) {
			this(customization.lookupFactory);
			this.eventHandler = customization.getEventHandler();
			this.serializationMode = customization.getSerializationMode();
			this.classDefiners = customization.getClassDefiners();
			this.instantiators = customization.getInstantiators();
			this.initializationMode = customization.getInitializationMode();
			this.generateBridgeMethods = customization.generateBridgeMethods();
		}

		Set<AvailableClassDefiner> getClassDefiners() {
			return Utils.or(this.classDefiners, DEFAULT_CLASS_DEFINERS);
		}

		private Set<AvailableInstantiator> getInstantiators() {
			return Utils.or(this.instantiators, DEFAULT_INSTANTIATORS);
		}

		AvailableSerializationMode getSerializationMode() {
			return Utils.or(serializationMode, DEFAULT_SERIALIZATION_MODE);
		}

		EventHandler getEventHandler() {
			return Utils.or(eventHandler, DEFAULT_EVENT_HANDLER);
		}

		AvailableInitializationMode getInitializationMode() {
			return Utils.or(this.initializationMode, DEFAULT_INITIALIZATION_MODE);
		}

		Boolean generateBridgeMethods() {
			return Utils.or(this.generateBridgeMethods, DEFAULT_GENERATE_BRIDGE_METHODS);
		}

		ImplSpec.ClassGeneratorConfig getClassGeneratorConfig() {
			return new ImplSpec.ClassGeneratorConfig(
					getSerializationMode(),
					getInitializationMode(),
					generateBridgeMethods());
		}

		Lookup getLookup(Class<?> classToCompare) {
			return lookupFactory.lookupFor(classToCompare);
		}

		@Override
		public String toString() {
			return "Customization[" +
					"lookupFactory=" + lookupFactory +
					", eventHandler=" + eventHandler +
					", serializationMode=" + serializationMode +
					", classDefiners=" + classDefiners +
					", instantiators=" + instantiators +
					", initializationMode=" + initializationMode +
					", generateBridgeMethods=" + generateBridgeMethods +
					"]";
		}
	}

	private enum NoopEventHandler implements EventHandler {

		INSTANCE

	}

	private static final class ClassDefinerHolder {

		private static final Lazy<VMAnonymousClassDefiner> VM_ANONYMOUS = new Lazy<>(VMAnonymousClassDefiner::new);

		private static final Lazy<LookupHiddenClassWithClassDataDefiner> LOOKUP_HIDDEN_CLASS_WITH_CLASS_DATA = new Lazy<>(LookupHiddenClassWithClassDataDefiner::new);

		private static final Lazy<LookupHiddenClassDefiner> LOOKUP_HIDDEN = new Lazy<>(LookupHiddenClassDefiner::new);

		private static final Lazy<LookupClassDefiner> LOOKUP = new Lazy<>(LookupClassDefiner::new);

		private static final Lazy<ClassLoaderClassDefiner> CLASS_LOADER = new Lazy<>(ClassLoaderClassDefiner::new);

		@Nullable
		VMAnonymousClassDefiner vmAnonymous(Collection<AvailableClassDefiner> availableClassDefiners) {
			return load(availableClassDefiners, AvailableClassDefiner.VM_ANONYMOUS, VM_ANONYMOUS);
		}

		@Nullable
		LookupHiddenClassWithClassDataDefiner lookupHiddenClassWithClassDataDefiner(Collection<AvailableClassDefiner> availableClassDefiners) {
			return load(availableClassDefiners, AvailableClassDefiner.LOOKUP_HIDDEN_CLASS_DATA, LOOKUP_HIDDEN_CLASS_WITH_CLASS_DATA);
		}

		@Nullable
		LookupHiddenClassDefiner lookupHiddenClassDefiner(Collection<AvailableClassDefiner> availableClassDefiners) {
			return load(availableClassDefiners, AvailableClassDefiner.LOOKUP_HIDDEN, LOOKUP_HIDDEN);
		}

		@Nullable
		LookupClassDefiner lookupClassDefiner(Collection<AvailableClassDefiner> availableClassDefiners) {
			return load(availableClassDefiners, AvailableClassDefiner.LOOKUP, LOOKUP);
		}

		@Nullable
		ClassLoaderClassDefiner classLoaderClassDefiner(Collection<AvailableClassDefiner> availableClassDefiners) {
			return load(availableClassDefiners, AvailableClassDefiner.CLASS_LOADER, CLASS_LOADER);
		}

		private <T> @Nullable T load(Collection<AvailableClassDefiner> available, AvailableClassDefiner wanted, Lazy<T> lazy) {
			try {
				return available.contains(wanted) ? lazy.load() : null;
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				LOGGER.log(Level.FINE, "ClassDefiner is unavailable: " + wanted, e);
				return null;
			}
		}

	}

	@ThreadSafe
	private static final class Lazy<T> {

		private volatile Callable<? extends T> factory;

		private T value;

		Lazy(Callable<? extends T> factory) {
			this.factory = Objects.requireNonNull(factory);
		}

		public T load() throws Exception {
			Callable<? extends T> factory = this.factory;

			return factory == null ? value : newInstance(factory);
		}

		private synchronized T newInstance(Callable<? extends T> factory) throws Exception {
			if (this.factory == null) {
				return value;
			}

			try {
				T value = factory.call();
				this.value = value;

				return value;
			} finally {
				this.factory = null;
			}
		}

	}

	@ThreadSafe
	private static final class ConstantLookupFactory implements LookupFactory {

		private final Lookup lookup;

		ConstantLookupFactory(Lookup lookup) {
			this.lookup = Objects.requireNonNull(lookup);
		}

		@Override
		public Lookup lookupFor(Class<?> clazz) {
			return lookup;
		}

		@Override
		public String toString() {
			return "ConstantLookupFactory[lookup=" + lookup + "]";
		}

	}

}
