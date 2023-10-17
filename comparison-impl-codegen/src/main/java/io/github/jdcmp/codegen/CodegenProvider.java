package io.github.jdcmp.codegen;

import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.provider.ComparatorProvider;
import io.github.jdcmp.api.provider.ComparatorProviders;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import io.github.jdcmp.codegen.ClassDefiners.ClassLoaderClassDefiner;
import io.github.jdcmp.codegen.ClassDefiners.LookupClassDefiner;
import io.github.jdcmp.codegen.ClassDefiners.LookupHiddenClassDefiner;
import io.github.jdcmp.codegen.ClassDefiners.LookupHiddenClassWithClassDataDefiner;
import io.github.jdcmp.codegen.ClassDefiners.VMAnonymousClassDefiner;
import io.github.jdcmp.codegen.ImplSpec.ClassGeneratorConfig;
import io.github.jdcmp.codegen.ImplSpec.OptionalClassDefiners;
import io.github.jdcmp.codegen.Instantiators.ConstructorInstantiator;
import io.github.jdcmp.codegen.Instantiators.ReflectionFactoryConstructorInstantiator;
import io.github.jdcmp.codegen.Instantiators.ReflectionFactoryInstantiator;
import io.github.jdcmp.codegen.Instantiators.UnsafeInstantiator;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This class is the ServiceProvider for <i>comparison-impl-codegen</i>, which is a concrete implementation of the abstract API
 * <i>comparison-api</i>.</p>
 *
 * <p>Instances are usually obtained via {@link ComparatorProviders#load(Class)}. However, if customization of the {@link LookupFactory}
 * is necessary, one of the {@link #of(LookupFactory)} or {@link #of(Lookup)} static factory methods may be used.</p>
 */
@ThreadSafe
@CarefulRefactoring(reason = "Public API")
public final class CodegenProvider implements ComparatorProvider {

	private static final Logger LOGGER = Logger.getLogger(CodegenProvider.class.getName());

	private static final LookupFactory DEFAULT_LOOKUP_FACTORY
			= new ConstantLookupFactory(GeneratedClassHolder.lookup(MethodHandles.lookup()));

	private final Customization customization;

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
	 * @param eventHandler An event handler; null to restore default settings
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
	 * Configures strategies for creating instances of generated comparators.
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
	 * @param generateBridgeMethods True to generate bridge methods; false to omit bridge methods; null to restore default settings
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
		OptionalClassDefiners classDefiners = createOptionalClassDefiners(availableClassDefiners);
		List<Instantiator> instantiators = createInstantiators(customization.getInstantiators());
		ClassGeneratorConfig classGeneratorConfig = customization.getClassGeneratorConfig();

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

	private static OptionalClassDefiners createOptionalClassDefiners(Collection<AvailableClassDefiner> classDefiners) {
		try {
			return new OptionalClassDefiners(
					ClassDefinerHolder.vmAnonymous(classDefiners),
					ClassDefinerHolder.lookupHiddenClassWithClassData(classDefiners),
					ClassDefinerHolder.lookupHidden(classDefiners),
					ClassDefinerHolder.lookup(classDefiners),
					ClassDefinerHolder.classLoader(classDefiners)
			);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("ClassDefiners are unavailable: " + classDefiners, e);
		}
	}

	private static List<Instantiator> createInstantiators(Collection<AvailableInstantiator> instantiators) {
		try {
			return Utils.nonEmptyArrayListOfNonNulls(
					InstantiatorHolder.unsafe(instantiators),
					InstantiatorHolder.reflectionFactory(instantiators),
					InstantiatorHolder.reflectionFactoryConstructor(instantiators),
					InstantiatorHolder.constructor(instantiators)
			);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Instantiators are unavailable: " + instantiators, e);
		}
	}

	@ThreadSafe
	private static final class Customization {

		static final EventHandler DEFAULT_EVENT_HANDLER = NoopEventHandler.INSTANCE;

		static final AvailableSerializationMode DEFAULT_SERIALIZATION_MODE = AvailableSerializationMode.COMPATIBLE;

		static final Set<AvailableClassDefiner> DEFAULT_CLASS_DEFINERS = Utils.immutableEnumSet(AvailableClassDefiner.class);

		static final Set<AvailableInstantiator> DEFAULT_INSTANTIATORS = Utils.immutableEnumSet(AvailableInstantiator.class);

		static final AvailableInitializationMode DEFAULT_INITIALIZATION_MODE = AvailableInitializationMode.STATIC_INITIALIZER;

		static final Boolean DEFAULT_GENERATE_BRIDGE_METHODS = Boolean.FALSE;

		final LookupFactory lookupFactory;

		volatile @Nullable EventHandler eventHandler;

		volatile @Nullable AvailableSerializationMode serializationMode;

		volatile @Nullable Set<AvailableClassDefiner> classDefiners;

		volatile @Nullable Set<AvailableInstantiator> instantiators;

		volatile @Nullable AvailableInitializationMode initializationMode;

		volatile @Nullable Boolean generateBridgeMethods;

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

		Set<AvailableInstantiator> getInstantiators() {
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

		ClassGeneratorConfig getClassGeneratorConfig() {
			return new ClassGeneratorConfig(
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

	@ThreadSafe
	private enum NoopEventHandler implements EventHandler {

		INSTANCE;

		@Override
		public String toString() {
			return NoopEventHandler.class.getSimpleName() + "." + name();
		}

	}

	@ThreadSafe
	private static final class ClassDefinerHolder {

		static final Lazy<VMAnonymousClassDefiner> VM_ANONYMOUS = new Lazy<>(VMAnonymousClassDefiner::new);

		static final Lazy<LookupHiddenClassWithClassDataDefiner> LOOKUP_HIDDEN_CLASS_WITH_CLASS_DATA
				= new Lazy<>(LookupHiddenClassWithClassDataDefiner::new);

		static final Lazy<LookupHiddenClassDefiner> LOOKUP_HIDDEN = new Lazy<>(LookupHiddenClassDefiner::new);

		static final Lazy<LookupClassDefiner> LOOKUP = new Lazy<>(LookupClassDefiner::new);

		static final Lazy<ClassLoaderClassDefiner> CLASS_LOADER = new Lazy<>(ClassLoaderClassDefiner::new);

		static @Nullable VMAnonymousClassDefiner vmAnonymous(Collection<AvailableClassDefiner> classDefiners) {
			return VM_ANONYMOUS.load(classDefiners, AvailableClassDefiner.VM_ANONYMOUS);
		}

		static @Nullable LookupHiddenClassWithClassDataDefiner lookupHiddenClassWithClassData(
				Collection<AvailableClassDefiner> classDefiners) {
			return LOOKUP_HIDDEN_CLASS_WITH_CLASS_DATA.load(classDefiners, AvailableClassDefiner.LOOKUP_HIDDEN_CLASS_DATA);
		}

		static @Nullable LookupHiddenClassDefiner lookupHidden(Collection<AvailableClassDefiner> classDefiners) {
			return LOOKUP_HIDDEN.load(classDefiners, AvailableClassDefiner.LOOKUP_HIDDEN);
		}

		static @Nullable LookupClassDefiner lookup(Collection<AvailableClassDefiner> classDefiners) {
			return LOOKUP.load(classDefiners, AvailableClassDefiner.LOOKUP);
		}

		static @Nullable ClassLoaderClassDefiner classLoader(Collection<AvailableClassDefiner> classDefiners) {
			return CLASS_LOADER.load(classDefiners, AvailableClassDefiner.CLASS_LOADER);
		}

	}

	@ThreadSafe
	private static final class InstantiatorHolder {

		static final Lazy<UnsafeInstantiator> UNSAFE = new Lazy<>(UnsafeInstantiator::new);

		static final Lazy<ReflectionFactoryInstantiator> REFLECTION_FACTORY
				= new Lazy<>(ReflectionFactoryInstantiator::new);

		static final Lazy<ReflectionFactoryConstructorInstantiator> REFLECTION_FACTORY_CONSTRUCTOR
				= new Lazy<>(ReflectionFactoryConstructorInstantiator::new);

		static final Lazy<ConstructorInstantiator> CONSTRUCTOR = new Lazy<>(ConstructorInstantiator::new);

		static @Nullable UnsafeInstantiator unsafe(Collection<AvailableInstantiator> instantiators) {
			return UNSAFE.load(instantiators, AvailableInstantiator.UNSAFE);
		}

		static @Nullable ReflectionFactoryInstantiator reflectionFactory(Collection<AvailableInstantiator> instantiators) {
			return REFLECTION_FACTORY.load(instantiators, AvailableInstantiator.REFLECTION_FACTORY);
		}

		static @Nullable ReflectionFactoryConstructorInstantiator reflectionFactoryConstructor(
				Collection<AvailableInstantiator> instantiators) {
			return REFLECTION_FACTORY_CONSTRUCTOR.load(instantiators, AvailableInstantiator.REFLECTION_FACTORY);
		}

		static @Nullable ConstructorInstantiator constructor(Collection<AvailableInstantiator> instantiators) {
			return CONSTRUCTOR.load(instantiators, AvailableInstantiator.CONSTRUCTOR);
		}

	}

	@ThreadSafe
	private static final class Lazy<T> {

		private volatile @Nullable Callable<? extends T> factory;

		private @Nullable T value;

		Lazy(Callable<? extends T> factory) {
			this.factory = Objects.requireNonNull(factory);
		}

		public <A extends Enum<A>> @Nullable T load(Collection<A> available, A wanted) {
			try {
				return available.contains(wanted) ? load() : null;
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				String msg = wanted.getClass().getSimpleName() + "." + wanted.name() + " is unavailable.";
				LOGGER.log(Level.FINE, msg, e);
				return null;
			}
		}

		public @Nullable T load() throws Exception {
			return factory == null ? value : newInstance();
		}

		private synchronized @Nullable T newInstance() throws Exception {
			Callable<? extends T> factory = this.factory;

			if (factory == null) {
				return value;
			}

			try {
				T value = factory.call();
				this.value = value;

				return value;
			} finally {
				this.factory = null; // throw only once, the next call will return null
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
