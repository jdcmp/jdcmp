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
import io.github.jdcmp.api.spec.equality.BaseEqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.BaseOrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A static factory that provides convenience methods for creating immutable {@link Spec Specs}.
 */
@ThreadSafe
public final class Specs {

	/**
	 * Creates an immutable spec for {@link EqualityComparator}.
	 *
	 * @param classToCompare    Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes       Strict type checking flag
	 * @param getters           Criteria for comparisons
	 * @param fallbackMode      Missing criteria handling; nullable
	 * @param lookup            An access context; nullable
	 * @param <T>               Type whose instances can be compared
	 * @return An immutable spec
	 */
	public static <T> EqualityComparatorSpec<T> equality(
			Class<T> classToCompare,
			HashParameters getHashParameters,
			boolean strictTypes,
			Iterable<? extends EqualityCriterion<? super T>> getters,
			@Nullable EqualityFallbackMode fallbackMode,
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
	 * @param <T>  Type whose instances can be compared
	 * @return An immutable copy
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
	 * @param <T>  Type whose instances can be compared
	 * @return An immutable copy
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
	 * @param classToCompare    Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes       Strict type checking flag
	 * @param getters           Criteria for comparisons
	 * @param fallbackMode      Missing criteria handling
	 * @param lookup            A nullable access context
	 * @param <T>               Type whose instances can be compared
	 * @return An immutable spec
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
	 * @param classToCompare    Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes       Strict type checking flag
	 * @param getters           Criteria for comparisons
	 * @param nullHandling      How nulls are handled
	 * @param fallbackMode      Missing criteria handling
	 * @param lookup            A nullable access context
	 * @param <T>               Type whose instances can be compared
	 * @return An immutable spec
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
	 * @param <T>  Type whose instances can be compared
	 * @return An immutable copy
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
	 * @param classToCompare    Type whose instances can be compared
	 * @param getHashParameters Customization for hashCode
	 * @param strictTypes       Strict type checking flag
	 * @param getters           Criteria for comparisons
	 * @param nullHandling      How nulls are handled
	 * @param fallbackMode      Missing criteria handling
	 * @param lookup            A nullable access context
	 * @param <T>               Type whose instances can be compared
	 * @return An immutable spec
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
	 * @param <T>  Type whose instances can be compared
	 * @return An immutable copy
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

	private static final class ImmutableEqualityComparatorSpec<T>
			extends AbstractEqualitySpec<T, EqualityCriterion<? super T>>
			implements EqualityComparatorSpec<T> {

		private final List<? extends EqualityCriterion<? super T>> getters;

		ImmutableEqualityComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends EqualityCriterion<? super T>> getters,
				@Nullable EqualityFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, false, strictTypes, fallbackMode, lookup);
			this.getters = immutableList(getters);
		}

		@Override
		public Collection<? extends EqualityCriterion<? super T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends EqualityCriterion<? super T>> getGettersAsList() {
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
			extends AbstractEqualitySpec<T, SerializableEqualityCriterion<? super T>>
			implements SerializableEqualityComparatorSpec<T> {

		private final List<? extends SerializableEqualityCriterion<? super T>> getters;

		ImmutableSerializableEqualityComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends SerializableEqualityCriterion<? super T>> getters,
				@Nullable EqualityFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, true, strictTypes, fallbackMode, lookup);
			this.getters = immutableList(getters);
		}

		@Override
		public Collection<? extends SerializableEqualityCriterion<? super T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends SerializableEqualityCriterion<? super T>> getGettersAsList() {
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

	private static final class ImmutableOrderingComparatorSpec<T>
			extends AbstractOrderingSpec<T, OrderingCriterion<? super T>>
			implements OrderingComparatorSpec<T> {

		private final List<? extends OrderingCriterion<? super T>> getters;

		public ImmutableOrderingComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends OrderingCriterion<? super T>> getters,
				NullHandling nullHandling,
				@Nullable OrderingFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, false, strictTypes, nullHandling, fallbackMode, lookup);
			this.getters = immutableList(getters);
		}

		@Override
		public Collection<? extends OrderingCriterion<? super T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends OrderingCriterion<? super T>> getGettersAsList() {
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
			extends AbstractOrderingSpec<T, SerializableOrderingCriterion<? super T>>
			implements SerializableOrderingComparatorSpec<T> {

		private final List<? extends SerializableOrderingCriterion<? super T>> getters;

		ImmutableSerializableOrderingComparatorSpec(
				Class<T> classToCompare,
				HashParameters hashParameters,
				boolean strictTypes,
				Iterable<? extends SerializableOrderingCriterion<? super T>> getters,
				NullHandling nullHandling,
				@Nullable OrderingFallbackMode fallbackMode,
				@Nullable Lookup lookup) {
			super(classToCompare, hashParameters, true, strictTypes, nullHandling, fallbackMode, lookup);
			this.getters = immutableList(getters);
		}

		@Override
		public Collection<? extends SerializableOrderingCriterion<? super T>> getGetters() {
			return getters;
		}

		@Override
		public List<? extends SerializableOrderingCriterion<? super T>> getGettersAsList() {
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
			extends AbstractSpec<T, G>
			implements BaseOrderingComparatorSpec<T, G> {

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
			extends AbstractSpec<T, G>
			implements BaseEqualityComparatorSpec<T, G> {

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

	static <T> List<T> immutableList(Iterable<? extends T> iterable) {
		if (iterable instanceof Collection) {
			Collection<? extends T> col = (Collection<? extends T>) iterable;
			ArrayList<T> list = new ArrayList<>(col);
			for (T element : list) {
				Objects.requireNonNull(element);
			}

			return Collections.unmodifiableList(list);
		}

		ArrayList<T> list = new ArrayList<>();
		for (T element : iterable) {
			list.add(Objects.requireNonNull(element));
		}

		return Collections.unmodifiableList(list);
	}

	private Specs() {
		throw new AssertionError("No instances");
	}

}
