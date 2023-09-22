package io.github.jdcmp.codegen;

import io.github.jdcmp.api.documentation.Immutable;
import io.github.jdcmp.codegen.ClassDefiners.ClassLoaderClassDefiner;
import io.github.jdcmp.codegen.ClassDefiners.LookupClassDefiner;
import io.github.jdcmp.codegen.ClassDefiners.LookupHiddenClassDefiner;
import io.github.jdcmp.codegen.ClassDefiners.LookupHiddenClassWithClassDataDefiner;
import io.github.jdcmp.codegen.ClassDefiners.VMAnonymousClassDefiner;
import io.github.jdcmp.codegen.contract.EventHandler;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Immutable // constructor dependencies are trusted to be immutable
final class ImplSpec {

	private final Lookup lookup;

	private final EventHandler eventHandler;

	private final OptionalClassDefiners classDefiners;

	private final List<Instantiator> instantiators;

	private final ClassGeneratorConfig classGenerationConfig;

	ImplSpec(
			Lookup lookup,
			EventHandler eventHandler,
			OptionalClassDefiners classDefiners,
			List<? extends Instantiator> instantiators,
			ClassGeneratorConfig classGeneratorConfig) {
		this.lookup = Objects.requireNonNull(lookup);
		this.eventHandler = Objects.requireNonNull(eventHandler);
		this.classDefiners = Objects.requireNonNull(classDefiners);
		this.instantiators = Collections.unmodifiableList(instantiators);
		this.classGenerationConfig = Objects.requireNonNull(classGeneratorConfig);
	}

	public Lookup getLookup() {
		return lookup;
	}

	public EventHandler getEventHandler() {
		return eventHandler;
	}

	public OptionalClassDefiners getClassDefiners() {
		return classDefiners;
	}

	public List<Instantiator> getInstantiators() {
		return instantiators;
	}

	public AvailableSerializationMode getSerializationMode() {
		return classGenerationConfig.serializationMode;
	}

	public AvailableInitializationMode getInitializationMode() {
		return classGenerationConfig.initializationMode;
	}

	public boolean generateBridgeMethods() {
		return classGenerationConfig.generateBridgeMethods;
	}

	@Immutable
	static final class OptionalClassDefiners {

		private final VMAnonymousClassDefiner vmAnonymousClassDefiner;

		private final LookupHiddenClassWithClassDataDefiner lookupHiddenClassWithClassDataDefiner;

		private final Set<ClassDefiner> all;

		public OptionalClassDefiners(
				@Nullable VMAnonymousClassDefiner vmAnonymousClassDefiner,
				@Nullable LookupHiddenClassWithClassDataDefiner lookupHiddenClassWithClassDataDefiner,
				@Nullable LookupHiddenClassDefiner lookupHiddenClassDefiner,
				@Nullable LookupClassDefiner lookupClassDefiner,
				@Nullable ClassLoaderClassDefiner classLoaderClassDefiner) throws IllegalArgumentException {
			this.vmAnonymousClassDefiner = vmAnonymousClassDefiner;
			this.lookupHiddenClassWithClassDataDefiner = lookupHiddenClassWithClassDataDefiner;
			LinkedHashSet<ClassDefiner> all = new LinkedHashSet<>(5);
			add(all, vmAnonymousClassDefiner);
			add(all, lookupHiddenClassWithClassDataDefiner);
			add(all, lookupHiddenClassDefiner);
			add(all, lookupClassDefiner);
			add(all, classLoaderClassDefiner);
			Utils.verify(all, Utils::isNotEmpty, () -> "At least one ClassDefiner must be available.");

			this.all = Collections.unmodifiableSet(all);
		}

		private static <T extends ClassDefiner> void add(LinkedHashSet<T> collection, @Nullable T value) {
			if (value != null) {
				collection.add(value);
			}
		}

		public Set<ClassDefiner> all() {
			return all;
		}

		public Optional<VMAnonymousClassDefiner> getVmAnonymousClassDefiner() {
			return Optional.ofNullable(vmAnonymousClassDefiner);
		}

		public Optional<LookupHiddenClassWithClassDataDefiner> getLookupHiddenClassWithClassDataDefiner() {
			return Optional.ofNullable(lookupHiddenClassWithClassDataDefiner);
		}

	}

	@Immutable
	static final class ClassGeneratorConfig {

		private final AvailableSerializationMode serializationMode;

		private final AvailableInitializationMode initializationMode;

		private final boolean generateBridgeMethods;

		public ClassGeneratorConfig(
				AvailableSerializationMode serializationMode,
				AvailableInitializationMode initializationMode,
				boolean generateBridgeMethods) {
			this.serializationMode = Objects.requireNonNull(serializationMode);
			this.initializationMode = Objects.requireNonNull(initializationMode);
			this.generateBridgeMethods = generateBridgeMethods;
		}

	}

}
