package io.github.jdcmp.api;

import io.github.jdcmp.api.builder.equality.EqualityComparatorBuilder;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.builder.equality.SerializableEqualityComparatorBuilder;
import io.github.jdcmp.api.builder.equality.StepMandatoryGetter;
import io.github.jdcmp.api.builder.equality.StepMandatorySerializableGetter;
import io.github.jdcmp.api.builder.equality.StepMissingCriteriaHandling;
import io.github.jdcmp.api.builder.equality.StepMissingCriteriaHandlingSerializable;
import io.github.jdcmp.api.builder.equality.StepSerializationEquality;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.EqualityCriterion;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.provider.ComparatorProvider;
import io.github.jdcmp.api.provider.ComparatorProviders;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ThreadSafe
final class EqualityComparators {

	private static final HashParameters DEFAULT_HASH_PARAMETERS = HashParameters.of(17, 37);

	static StepSerializationEquality builder() {
		return SerializationDecision.INSTANCE;
	}

	private enum SerializationDecision implements StepSerializationEquality {

		INSTANCE;

		@Override
		public StepMissingCriteriaHandling nonSerializable() {
			return MissingCriteriaDecision.INSTANCE;
		}

		@Override
		public StepMissingCriteriaHandlingSerializable serializable() {
			return MissingCriteriaDecisionSerializable.INSTANCE;
		}

	}

	private enum MissingCriteriaDecision implements StepMissingCriteriaHandling {

		INSTANCE;

		@Override
		public <T> StepMandatoryGetter<T> requireAtLeastOneGetter(Class<T> classToCompare) {
			return new RequireAtLeastOneGetter<>(classToCompare);
		}

		@Override
		public <T> EqualityComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare) {
			return new Builder<>(classToCompare, EqualityFallbackMode.IDENTITY);
		}

	}

	private enum MissingCriteriaDecisionSerializable implements StepMissingCriteriaHandlingSerializable {

		INSTANCE;

		@Override
		public <T> StepMandatorySerializableGetter<T> requireAtLeastOneGetter(Class<T> classToCompare) {
			return new RequireAtLeastOneSerializableGetter<>(classToCompare);
		}

		@Override
		public <T> SerializableEqualityComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare) {
			return new SerializableBuilder<>(classToCompare, EqualityFallbackMode.IDENTITY);
		}

	}

	private static final class RequireAtLeastOneGetter<T> implements StepMandatoryGetter<T> {

		private final Class<T> classToCompare;

		RequireAtLeastOneGetter(Class<T> classToCompare) {
			this.classToCompare = Utils.verifyClassNotForbidden(classToCompare);
		}

		@Override
		public EqualityComparatorBuilder<T> use(EqualityCriterion<? super T> getter) {
			return new Builder<>(classToCompare, null).use(getter);
		}

		@Override
		public EqualityComparatorBuilder<T> use(Iterable<? extends EqualityCriterion<? super T>> getters) {
			List<EqualityCriterion<? super T>> copy = Utils.arrayList(getters);

			if (copy.isEmpty()) {
				throw MissingCriteriaException.of();
			}

			return new Builder<>(classToCompare, null).use(copy);
		}

	}

	private static final class RequireAtLeastOneSerializableGetter<T> implements StepMandatorySerializableGetter<T> {

		private final Class<T> classToCompare;

		RequireAtLeastOneSerializableGetter(Class<T> classToCompare) {
			this.classToCompare = Utils.verifyClassNotForbidden(classToCompare);
		}

		@Override
		public SerializableEqualityComparatorBuilder<T> use(SerializableEqualityCriterion<? super T> getter) {
			Objects.requireNonNull(getter);

			return new SerializableBuilder<>(classToCompare, null).use(getter);
		}

		@Override
		public SerializableEqualityComparatorBuilder<T> use(Iterable<? extends SerializableEqualityCriterion<? super T>> getters) {
			List<SerializableEqualityCriterion<? super T>> copy = Utils.arrayList(getters);

			if (copy.isEmpty()) {
				throw MissingCriteriaException.of();
			}

			return new SerializableBuilder<>(classToCompare, null).use(copy);
		}

	}

	static final class Builder<T>
			extends
			AbstractBuilder<T,
					EqualityComparatorBuilder<T>,
					EqualityComparator<T>,
					EqualityCriterion<? super T>>
			implements
			EqualityComparatorBuilder<T> {

		Builder(Class<T> classToCompare, @Nullable EqualityFallbackMode fallbackMode) {
			super(classToCompare, fallbackMode);
		}

		@Override
		protected EqualityComparatorBuilder<T> getThis() {
			return this;
		}

		protected EqualityComparator<T> create(ComparatorProvider comparatorProvider, Lookup lookup) {
			EqualityComparatorSpec<T> spec = Specs.equality(
					classToCompare,
					Utils.orNonNull(this.hashParameters, DEFAULT_HASH_PARAMETERS),
					strictTypes,
					getters,
					fallbackMode,
					lookup);

			return comparatorProvider.createEqualityComparator(spec);
		}

	}

	static final class SerializableBuilder<T>
			extends
			AbstractBuilder<T,
					SerializableEqualityComparatorBuilder<T>,
					SerializableEqualityComparator<T>,
					SerializableEqualityCriterion<? super T>>
			implements
			SerializableEqualityComparatorBuilder<T> {

		SerializableBuilder(Class<T> classToCompare, @Nullable EqualityFallbackMode fallbackMode) {
			super(classToCompare, fallbackMode);
		}

		@Override
		protected SerializableEqualityComparatorBuilder<T> getThis() {
			return this;
		}

		protected SerializableEqualityComparator<T> create(ComparatorProvider comparatorProvider, @Nullable Lookup lookup) {
			SerializableEqualityComparatorSpec<T> spec = Specs.equalitySerializable(
					classToCompare,
					Utils.orNonNull(this.hashParameters, DEFAULT_HASH_PARAMETERS),
					strictTypes,
					getters,
					fallbackMode,
					lookup);

			return comparatorProvider.createSerializableEqualityComparator(spec);
		}

	}

	private static abstract class AbstractBuilder<
			T,
			B,
			C extends EqualityComparator<T>,
			G extends EqualityCriterion<? super T>> {

		protected final Class<T> classToCompare;

		protected final @Nullable EqualityFallbackMode fallbackMode;

		protected final List<G> getters = new ArrayList<>();

		protected @Nullable HashParameters hashParameters;

		protected boolean strictTypes = true;

		protected AbstractBuilder(Class<T> classToCompare, @Nullable EqualityFallbackMode fallbackMode) {
			this.classToCompare = Utils.verifyClassNotForbidden(classToCompare);
			this.fallbackMode = fallbackMode;
		}

		abstract protected C create(ComparatorProvider provider, @Nullable Lookup lookup);

		abstract protected B getThis();

		public B hashParameters(HashParameters hashParameters) {
			this.hashParameters = Objects.requireNonNull(hashParameters);
			return getThis();
		}

		public B strictTypes(boolean strictTypes) {
			this.strictTypes = strictTypes;
			return getThis();
		}

		public B use(G getter) {
			Objects.requireNonNull(getter);
			this.getters.add(getter);
			return getThis();
		}

		public B use(Iterable<? extends G> getters) {
			getters.forEach(this::use);
			return getThis();
		}

		public C build() {
			return create(ComparatorProviders.load(), null);
		}

		public C build(ComparatorProvider provider) {
			return create(Objects.requireNonNull(provider), null);
		}

		public C build(ComparatorProvider provider, Lookup lookup) {
			Objects.requireNonNull(provider);
			Objects.requireNonNull(lookup);

			return create(provider, lookup);
		}

		@Override
		public String toString() {
			return "AbstractBuilder[" +
					"classToCompare=" + classToCompare +
					", fallbackMode=" + fallbackMode +
					", getters=" + getters +
					", hashParameters=" + hashParameters +
					", strictTypes=" + strictTypes +
					"]";
		}

	}

	private EqualityComparators() {
		throw new AssertionError("No instances");
	}

}
