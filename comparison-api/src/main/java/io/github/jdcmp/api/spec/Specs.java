package io.github.jdcmp.api.spec;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.EqualityCriterion;
import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.serialization.EqualityComparatorSerializedForm;
import io.github.jdcmp.api.serialization.OrderingComparatorSerializedForm;
import io.github.jdcmp.api.serialization.SerializationSupport;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.*;

/**
 * A static factory that provides convenience methods for creating immutable {@link Spec Specs}.
 */
@ThreadSafe
public final class Specs {

	/**
	 * Creates an immutable spec for {@link EqualityComparator}.
	 *
	 * @param classToCompare Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes Strict type checking flag
	 * @param getters Criteria for comparisons
	 * @param fallbackMode Missing criteria handling; nullable
	 * @param lookup An access context; nullable
	 * @return An immutable spec
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> EqualityComparatorSpec<T> equality(
			Class<T> classToCompare,
			HashParameters getHashParameters,
			boolean strictTypes,
			Iterable<? extends EqualityCriterion<? super T>> getters,
			@Nullable  EqualityFallbackMode fallbackMode,
			@Nullable Lookup lookup) {
		return new ImmutableEqualityComparatorSpec<>(
				classToCompare,
				getHashParameters,
				strictTypes,
				getters,
				fallbackMode,
				lookup);
	}

	/**
	 * Creates an immutable copy of the given spec.
	 *
	 * @param spec Spec to be copied
	 * @return An immutable copy
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> EqualityComparatorSpec<T> equality(EqualityComparatorSpec<T> spec) {
		return equality(
				spec.getClassToCompare(),
				spec.getHashParameters(),
				spec.useStrictTypes(),
				spec.getGetters(),
				spec.getFallbackMode().orElse(null),
				spec.getLookup().orElse(null)
		);
	}

	/**
	 * Creates an immutable copy of the given spec.
	 *
	 * @param spec Spec to be copied
	 * @return An immutable copy
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> SerializableEqualityComparatorSpec<T> equalitySerializable(
			SerializableEqualityComparatorSpec<T> spec) {
		return equalitySerializable(
				spec.getClassToCompare(),
				spec.getHashParameters(),
				spec.useStrictTypes(),
				spec.getGetters(),
				spec.getFallbackMode().orElse(null),
				spec.getLookup().orElse(null)
		);
	}

	/**
	 * Creates an immutable spec for {@link SerializableEqualityComparator}.
	 *
	 * @param classToCompare Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes Strict type checking flag
	 * @param getters Criteria for comparisons
	 * @param fallbackMode Missing criteria handling
	 * @param lookup A nullable access context
	 * @return An immutable spec
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> SerializableEqualityComparatorSpec<T> equalitySerializable(
			Class<T> classToCompare,
			HashParameters getHashParameters,
			boolean strictTypes,
			Iterable<? extends SerializableEqualityCriterion<? super T>> getters,
			EqualityFallbackMode fallbackMode,
			@Nullable Lookup lookup) {
		return new ImmutableSerializableEqualityComparatorSpec<>(
				classToCompare,
				getHashParameters,
				strictTypes,
				getters,
				fallbackMode,
				lookup);
	}

	/**
	 * Creates an immutable spec for {@link OrderingComparator}.
	 *
	 * @param classToCompare Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes Strict type checking flag
	 * @param getters Criteria for comparisons
	 * @param nullHandling How nulls are handled
	 * @param fallbackMode Missing criteria handling
	 * @param lookup A nullable access context
	 * @return An immutable spec
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> OrderingComparatorSpec<T> ordering(
			Class<T> classToCompare,
			HashParameters getHashParameters,
			boolean strictTypes,
			Iterable<? extends OrderingCriterion<? super T>> getters,
			NullHandling nullHandling,
			OrderingFallbackMode fallbackMode,
			@Nullable Lookup lookup) {
		return new ImmutableOrderingComparatorSpec<>(
				classToCompare,
				getHashParameters,
				strictTypes,
				getters,
				nullHandling,
				fallbackMode,
				lookup);
	}

	/**
	 * Creates an immutable copy of the given spec.
	 *
	 * @param spec Spec to be copied
	 * @return An immutable copy
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> OrderingComparatorSpec<T> ordering(OrderingComparatorSpec<T> spec) {
		return ordering(
				spec.getClassToCompare(),
				spec.getHashParameters(),
				spec.useStrictTypes(),
				spec.getGetters(),
				spec.getNullHandling(),
				spec.getFallbackMode().orElse(null),
				spec.getLookup().orElse(null)
		);
	}

	/**
	 * Creates an immutable spec for {@link SerializableOrderingComparator}.
	 *
	 * @param classToCompare Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes Strict type checking flag
	 * @param getters Criteria for comparisons
	 * @param nullHandling How nulls are handled
	 * @param fallbackMode Missing criteria handling
	 * @param lookup A nullable access context
	 * @return An immutable spec
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> SerializableOrderingComparatorSpec<T> orderingSerializable(
			Class<T> classToCompare,
			HashParameters getHashParameters,
			boolean strictTypes,
			Iterable<? extends SerializableOrderingCriterion<? super T>> getters,
			NullHandling nullHandling,
			OrderingFallbackMode fallbackMode,
			@Nullable Lookup lookup) {
		return new ImmutableSerializableOrderingComparatorSpec<>(
				classToCompare,
				getHashParameters,
				strictTypes,
				getters,
				nullHandling,
				fallbackMode,
				lookup);
	}

	/**
	 * Creates an immutable copy of the given spec.
	 *
	 * @param spec Spec to be copied
	 * @return An immutable copy
	 * @param <T> Type whose instances can be compared
	 */
	public static <T> SerializableOrderingComparatorSpec<T> orderingSerializable(SerializableOrderingComparatorSpec<T> spec) {
		return orderingSerializable(
				spec.getClassToCompare(),
				spec.getHashParameters(),
				spec.useStrictTypes(),
				spec.getGetters(),
				spec.getNullHandling(),
				spec.getFallbackMode().orElse(null),
				spec.getLookup().orElse(null)
		);
	}

	private static final class ImmutableEqualityComparatorSpec<T> extends AbstractEqualitySpec<T, EqualityCriterion<T>>
			implements EqualityComparatorSpec<T> {

		private final List<? extends EqualityCriterion<T>> getters;

		ImmutableEqualityComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends EqualityCriterion<? super T>> getters,
				@Nullable EqualityFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, false, strictTypes, fallbackMode, lookup);
			this.getters = Collections.unmodifiableList(asCastedList(getters));
		}

		@SuppressWarnings("unchecked")
		private List<EqualityCriterion<T>> asCastedList(Iterable<? extends EqualityCriterion<? super T>> getters) {
			ArrayList<EqualityCriterion<T>> list = new ArrayList<>();
			for (EqualityCriterion<? super T> getter : getters) {
				list.add((EqualityCriterion<T>) Objects.requireNonNull(getter));
			}

			return list;
		}

		@Override
		public Collection<? extends EqualityCriterion<T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends EqualityCriterion<T>> getGettersAsList() {
			return getters;
		}

		@Override
		public String toString() {
			return "ImmutableEqualityComparatorSpec[" +
					"fallbackMode=" + fallbackMode +
					", classToCompare=" + classToCompare +
					", hashParameters=" + hashParameters +
					", serializable=" + serializable +
					", strictTypes=" + strictTypes +
					", lookup=" + lookup +
					"]";
		}

	}

	private static final class ImmutableSerializableEqualityComparatorSpec<T>
			extends AbstractEqualitySpec<T, SerializableEqualityCriterion<T>>
			implements SerializableEqualityComparatorSpec<T> {

		private final List<? extends SerializableEqualityCriterion<T>> getters;

		ImmutableSerializableEqualityComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends SerializableEqualityCriterion<? super T>> getters,
				@Nullable EqualityFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, true, strictTypes, fallbackMode, lookup);
			this.getters = Collections.unmodifiableList(asCastedList(getters));
		}

		@SuppressWarnings("unchecked")
		private List<SerializableEqualityCriterion<T>> asCastedList(
				Iterable<? extends SerializableEqualityCriterion<? super T>> getters) {
			ArrayList<SerializableEqualityCriterion<T>> list = new ArrayList<>();
			for (SerializableEqualityCriterion<? super T> getter : getters) {
				list.add((SerializableEqualityCriterion<T>) Objects.requireNonNull(getter));
			}

			return list;
		}

		@Override
		public Collection<? extends SerializableEqualityCriterion<T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends SerializableEqualityCriterion<T>> getGettersAsList() {
			return getters;
		}

		@Override
		public EqualityComparatorSerializedForm<T> toSerializedForm() {
			return SerializationSupport.equality(this);
		}

		@Override
		public String toString() {
			return "ImmutableSerializableEqualityComparatorSpec[" +
					"fallbackMode=" + fallbackMode +
					", classToCompare=" + classToCompare +
					", hashParameters=" + hashParameters +
					", serializable=" + serializable +
					", strictTypes=" + strictTypes +
					", lookup=" + lookup +
					"]";
		}
	}

	private static final class ImmutableOrderingComparatorSpec<T> extends AbstractOrderingSpec<T, OrderingCriterion<T>>
			implements OrderingComparatorSpec<T> {

		private final List<? extends OrderingCriterion<T>> getters;

		public ImmutableOrderingComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends OrderingCriterion<? super T>> getters,
				NullHandling nullHandling,
				@Nullable OrderingFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, false, strictTypes, nullHandling, fallbackMode, lookup);
			this.getters = Collections.unmodifiableList(asCastedList(getters));
		}

		@SuppressWarnings("unchecked")
		private List<OrderingCriterion<T>> asCastedList(Iterable<? extends OrderingCriterion<? super T>> getters) {
			ArrayList<OrderingCriterion<T>> list = new ArrayList<>();
			for (OrderingCriterion<? super T> getter : getters) {
				list.add((OrderingCriterion<T>) Objects.requireNonNull(getter));
			}

			return list;
		}

		@Override
		public Collection<? extends OrderingCriterion<T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends OrderingCriterion<T>> getGettersAsList() {
			return getters;
		}

		@Override
		public String toString() {
			return "ImmutableOrderingComparatorSpec[" +
					"nullHandling=" + nullHandling +
					", fallbackMode=" + fallbackMode +
					", classToCompare=" + classToCompare +
					", hashParameters=" + hashParameters +
					", serializable=" + serializable +
					", strictTypes=" + strictTypes +
					", lookup=" + lookup +
					"]";
		}
	}

	private static final class ImmutableSerializableOrderingComparatorSpec<T>
			extends AbstractOrderingSpec<T, SerializableOrderingCriterion<T>>
			implements SerializableOrderingComparatorSpec<T> {

		private final List<? extends SerializableOrderingCriterion<T>> getters;

		ImmutableSerializableOrderingComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends SerializableOrderingCriterion<? super T>> getters,
				NullHandling nullHandling,
				@Nullable OrderingFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, true, strictTypes, nullHandling, fallbackMode, lookup);
			this.getters = Collections.unmodifiableList(asCastedList(getters));
		}

		@SuppressWarnings("unchecked")
		private List<SerializableOrderingCriterion<T>> asCastedList(
				Iterable<? extends SerializableOrderingCriterion<? super T>> getters) {
			ArrayList<SerializableOrderingCriterion<T>> list = new ArrayList<>();
			for (SerializableOrderingCriterion<? super T> getter : getters) {
				list.add((SerializableOrderingCriterion<T>) Objects.requireNonNull(getter));
			}

			return list;
		}

		@Override
		public Collection<? extends SerializableOrderingCriterion<T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends SerializableOrderingCriterion<T>> getGettersAsList() {
			return getters;
		}

		@Override
		public OrderingComparatorSerializedForm<T> toSerializedForm() {
			return SerializationSupport.ordering(this);
		}

		@Override
		public String toString() {
			return "ImmutableSerializableOrderingComparatorSpec[" +
					"nullHandling=" + nullHandling +
					", fallbackMode=" + fallbackMode +
					", classToCompare=" + classToCompare +
					", hashParameters=" + hashParameters +
					", serializable=" + serializable +
					", strictTypes=" + strictTypes +
					", lookup=" + lookup +
					"]";
		}

	}

	private static abstract class AbstractOrderingSpec<T, G extends OrderingCriterion<? super T>>
			extends AbstractSpec<T, G> {

		protected final NullHandling nullHandling;

		protected final @Nullable OrderingFallbackMode fallbackMode;

		protected AbstractOrderingSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean serializable,
				boolean strictTypes,
				NullHandling nullHandling,
				@Nullable OrderingFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, serializable, strictTypes, lookup);
			this.nullHandling = Objects.requireNonNull(nullHandling);
			this.fallbackMode = fallbackMode;
		}

		public final NullHandling getNullHandling() {
			return nullHandling;
		}

		public final Optional<OrderingFallbackMode> getFallbackMode() {
			return Optional.ofNullable(fallbackMode);
		}

	}

	private static abstract class AbstractEqualitySpec<T, G extends EqualityCriterion<? super T>>
			extends AbstractSpec<T, G> {

		protected final @Nullable EqualityFallbackMode fallbackMode;

		protected AbstractEqualitySpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean serializable,
				boolean strictTypes,
				@Nullable EqualityFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, serializable, strictTypes, lookup);
			this.fallbackMode = fallbackMode;
		}

		public final Optional<EqualityFallbackMode> getFallbackMode() {
			return Optional.ofNullable(fallbackMode);
		}

	}

	private static abstract class AbstractSpec<T, G extends EqualityCriterion<? super T>> implements Spec<T, G> {

		protected final Class<T> classToCompare;

		protected final HashParameters hashParameters;

		protected final boolean serializable;

		protected final boolean strictTypes;

		protected final @Nullable Lookup lookup;

		protected AbstractSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean serializable,
				boolean strictTypes,
				@Nullable Lookup lookup) {
			this.classToCompare = Objects.requireNonNull(classToCompare);
			this.hashParameters = Objects.requireNonNull(hashParameters);
			this.serializable = serializable;
			this.strictTypes = strictTypes;
			this.lookup = lookup;
		}

		public final Class<T> getClassToCompare() {
			return classToCompare;
		}

		public final HashParameters getHashParameters() {
			return hashParameters;
		}

		public final boolean isSerializable() {
			return serializable;
		}

		@Override
		public final boolean useStrictTypes() {
			return strictTypes;
		}

		@Override
		public Optional<Lookup> getLookup() {
			return Optional.ofNullable(lookup);
		}

	}

	private Specs() {
		throw new AssertionError("No instances");
	}

}
