package io.github.jdcmp.api;

import io.github.jdcmp.api.builder.ordering.OrderingComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.builder.ordering.SerializableOrderingComparatorBuilder;
import io.github.jdcmp.api.builder.ordering.StepMandatoryGetter;
import io.github.jdcmp.api.builder.ordering.StepMandatorySerializableGetter;
import io.github.jdcmp.api.builder.ordering.StepMissingCriteriaHandling;
import io.github.jdcmp.api.builder.ordering.StepMissingCriteriaHandlingSerializable;
import io.github.jdcmp.api.builder.ordering.StepSerializationOrdering;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.NotThreadSafe;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.provider.ComparatorProvider;
import io.github.jdcmp.api.provider.ComparatorProviders;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ThreadSafe
final class OrderingComparators {

	private static final HashParameters DEFAULT_HASH_PARAMETERS = HashParameters.of(17, 37);

	static StepSerializationOrdering builder() {
		return SerializationDecision.INSTANCE;
	}

	private enum SerializationDecision implements StepSerializationOrdering {

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
		public <T> OrderingComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare) {
			return new Builder<>(classToCompare, OrderingFallbackMode.IDENTITY);
		}

		@Override
		public <T extends Comparable<? super T>> OrderingComparatorBuilder<T> fallbackToNaturalOrdering(Class<T> classToCompare) {
			return new Builder<>(classToCompare, OrderingFallbackMode.NATURAL);
		}

	}

	private enum MissingCriteriaDecisionSerializable implements StepMissingCriteriaHandlingSerializable {

		INSTANCE;

		@Override
		public <T> StepMandatorySerializableGetter<T> requireAtLeastOneGetter(Class<T> classToCompare) {
			return new RequireAtLeastOneSerializableGetter<>(classToCompare);
		}

		@Override
		public <T> SerializableOrderingComparatorBuilder<T> fallbackToIdentity(Class<T> classToCompare) {
			return new SerializableBuilder<>(classToCompare, OrderingFallbackMode.IDENTITY);
		}

		@Override
		public <T extends Comparable<? super T>> SerializableOrderingComparatorBuilder<T> fallbackToNaturalOrdering(Class<T> classToCompare) {
			return new SerializableBuilder<>(classToCompare, OrderingFallbackMode.NATURAL);
		}

	}

	private static final class RequireAtLeastOneGetter<T> implements StepMandatoryGetter<T> {

		private final Class<T> classToCompare;

		RequireAtLeastOneGetter(Class<T> classToCompare) {
			this.classToCompare = Utils.verifyClassNotForbidden(classToCompare);
		}

		@Override
		public OrderingComparatorBuilder<T> use(OrderingCriterion<? super T> getter) {
			return new Builder<>(classToCompare, null).use(getter);
		}

		@Override
		public OrderingComparatorBuilder<T> use(Iterable<? extends OrderingCriterion<? super T>> getters) {
			List<OrderingCriterion<? super T>> copy = Utils.arrayList(getters);

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
		public SerializableOrderingComparatorBuilder<T> use(SerializableOrderingCriterion<? super T> getter) {
			Objects.requireNonNull(getter);

			return new SerializableBuilder<>(classToCompare, null).use(getter);
		}

		@Override
		public SerializableOrderingComparatorBuilder<T> use(Iterable<? extends SerializableOrderingCriterion<? super T>> getters) {
			List<SerializableOrderingCriterion<? super T>> copy = Utils.arrayList(getters);

			if (copy.isEmpty()) {
				throw MissingCriteriaException.of();
			}

			return new SerializableBuilder<>(classToCompare, null).use(copy);
		}

	}

	@NotThreadSafe
	static final class Builder<T>
			extends
			AbstractBuilder<T,
					OrderingComparatorBuilder<T>,
					OrderingComparator<T>,
					OrderingCriterion<? super T>>
			implements
			OrderingComparatorBuilder<T> {

		Builder(Class<T> classToCompare, @Nullable OrderingFallbackMode fallbackMode) {
			super(classToCompare, fallbackMode);
		}

		@Override
		protected OrderingComparatorBuilder<T> getThis() {
			return this;
		}

		protected OrderingComparator<T> create(ComparatorProvider comparatorProvider, @Nullable Lookup lookup) {
			OrderingComparatorSpec<T> spec = Specs.ordering(
					classToCompare,
					Utils.orNonNull(this.hashParameters, DEFAULT_HASH_PARAMETERS),
					strictTypes,
					getters,
					Utils.orNonNull(this.nullHandling, NullHandling.THROW),
					fallbackMode,
					lookup);

			return comparatorProvider.createOrderingComparator(spec);
		}

	}

	@NotThreadSafe
	static final class SerializableBuilder<T> extends
			AbstractBuilder<T,
					SerializableOrderingComparatorBuilder<T>,
					SerializableOrderingComparator<T>,
					SerializableOrderingCriterion<? super T>>
			implements
			SerializableOrderingComparatorBuilder<T> {

		SerializableBuilder(Class<T> classToCompare, @Nullable OrderingFallbackMode fallbackMode) {
			super(classToCompare, fallbackMode);
		}

		@Override
		protected SerializableOrderingComparatorBuilder<T> getThis() {
			return this;
		}

		protected SerializableOrderingComparator<T> create(ComparatorProvider comparatorProvider, Lookup lookup) {
			SerializableOrderingComparatorSpec<T> spec = Specs.orderingSerializable(
					classToCompare,
					Utils.orNonNull(this.hashParameters, DEFAULT_HASH_PARAMETERS),
					strictTypes,
					getters,
					Utils.orNonNull(this.nullHandling, NullHandling.THROW),
					fallbackMode,
					lookup);

			return comparatorProvider.createSerializableOrderingComparator(spec);
		}

	}

	private static abstract class AbstractBuilder<
			T,
			B,
			C extends OrderingComparator<T>,
			G extends OrderingCriterion<? super T>> {

		protected final Class<T> classToCompare;

		protected final @Nullable OrderingFallbackMode fallbackMode;

		protected final List<G> getters = new ArrayList<>();

		protected @Nullable HashParameters hashParameters;

		protected boolean strictTypes = true;

		protected @Nullable NullHandling nullHandling;

		protected AbstractBuilder(Class<T> classToCompare, @Nullable OrderingFallbackMode fallbackMode) {
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

		public B nullHandling(NullHandling nullHandling) {
			this.nullHandling = Objects.requireNonNull(nullHandling);
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

	private OrderingComparators() {
		throw new AssertionError("No instances");
	}

}
