package io.github.jdcmp.api.serialization;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.MissingCriteriaException;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.provider.ComparatorProviders;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Provides static helper methods for serialization.
 */
@ThreadSafe
public final class SerializationSupport {

	/**
	 * Reconstructs a {@link SerializableEqualityComparator}.
	 *
	 * @param serializedForm Comparator's serialized form
	 * @param <T>            Type whose instances can be compared
	 * @return Reconstructed comparator
	 */
	public static <T> SerializableEqualityComparator<T> resolve(EqualityComparatorSerializedForm<T> serializedForm) {
		return EqualityResolver.resolve(serializedForm);
	}

	/**
	 * Reconstructs a {@link SerializableOrderingComparator}.
	 *
	 * @param serializedForm Comparator's serialized form
	 * @param <T>            Type whose instances can be compared
	 * @return Reconstructed comparator
	 */
	public static <T> SerializableOrderingComparator<T> resolve(OrderingComparatorSerializedForm<T> serializedForm) {
		return OrderingResolver.resolve(serializedForm);
	}

	/**
	 * Creates a serialized form of the given spec.
	 *
	 * @param spec Spec
	 * @param <T>  Type whose instances can be compared
	 * @return Serialized form of spec
	 */
	public static <T> EqualityComparatorSerializedForm<T> equality(SerializableEqualityComparatorSpec<T> spec) {
		return new EqualityComparatorSerializedFormV1<>(spec);
	}

	/**
	 * Creates a serialized form of the given spec.
	 *
	 * @param spec Spec
	 * @param <T>  Type whose instances can be compared
	 * @return Serialized form of spec
	 */
	public static <T> OrderingComparatorSerializedForm<T> ordering(SerializableOrderingComparatorSpec<T> spec) {
		return new OrderingComparatorSerializedFormV1<>(spec);
	}

	private static final class EqualityResolver {

		static <T> SerializableEqualityComparator<T> resolve(EqualityComparatorSerializedForm<T> serializedForm) {
			List<SerializableEqualityCriterion<T>> getters = Arrays.asList(serializedForm.getGetters().clone());

			if (!getters.isEmpty()) {
				return resolveNoFallback(serializedForm, getters);
			}

			EqualityFallbackMode fallbackMode = serializedForm.getFallbackMode()
					.orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new EqualityFallbackMode.FallbackMapper<SerializableEqualityComparator<T>>() {
				@Override
				public SerializableEqualityComparator<T> onIdentity() {
					return resolveIdentityFallback(serializedForm, getters);
				}
			});
		}

		private static <T> SerializableEqualityComparator<T> resolveNoFallback(
				EqualityComparatorSerializedForm<T> serializedForm,
				Iterable<SerializableEqualityCriterion<T>> getters) {
			return Comparators.equality()
					.serializable()
					.requireAtLeastOneGetter(serializedForm.getClassToCompare())
					.use(getters)
					.hashParameters(serializedForm.getHashParameters())
					.build(ComparatorProviders.loadForSerialization());
		}

		private static <T> SerializableEqualityComparator<T> resolveIdentityFallback(
				EqualityComparatorSerializedForm<T> serializedForm,
				Iterable<SerializableEqualityCriterion<T>> getters) {
			return Comparators.equality()
					.serializable()
					.fallbackToIdentity(serializedForm.getClassToCompare())
					.hashParameters(serializedForm.getHashParameters())
					.use(getters)
					.build(ComparatorProviders.loadForSerialization());
		}

	}

	private static final class OrderingResolver {

		static <T> SerializableOrderingComparator<T> resolve(OrderingComparatorSerializedForm<T> serializedForm) {
			List<SerializableOrderingCriterion<T>> getters = Arrays.asList(serializedForm.getGetters().clone());

			if (!getters.isEmpty()) {
				return resolveNoFallback(serializedForm, getters);
			}

			OrderingFallbackMode fallbackMode = serializedForm.getFallbackMode().orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new OrderingFallbackMode.FallbackMapper<SerializableOrderingComparator<T>>() {
				@Override
				public SerializableOrderingComparator<T> onIdentity() {
					return resolveIdentityFallback(serializedForm);
				}

				@Override
				public SerializableOrderingComparator<T> onNatural() {
					return resolveNaturalFallback(serializedForm);
				}
			});
		}

		private static <T> SerializableOrderingComparator<T> resolveNoFallback(
				OrderingComparatorSerializedForm<T> serializedForm,
				Iterable<SerializableOrderingCriterion<T>> getters) {
			return Comparators.ordering()
					.serializable()
					.requireAtLeastOneGetter(serializedForm.getClassToCompare())
					.use(getters)
					.nullHandling(serializedForm.getNullHandling())
					.hashParameters(serializedForm.getHashParameters())
					.build(ComparatorProviders.loadForSerialization());
		}

		private static <T> SerializableOrderingComparator<T> resolveIdentityFallback(
				OrderingComparatorSerializedForm<T> serializedForm) {
			return Comparators.ordering()
					.serializable()
					.fallbackToIdentity(serializedForm.getClassToCompare())
					.nullHandling(serializedForm.getNullHandling())
					.hashParameters(serializedForm.getHashParameters())
					.build(ComparatorProviders.loadForSerialization());
		}

		private static <T> SerializableOrderingComparator<T> resolveNaturalFallback(
				OrderingComparatorSerializedForm<T> serializedForm) {
			Class<T> classToCompare = serializedForm.getClassToCompare();

			if (!supportsNaturalOrdering(classToCompare)) {
				throw new IllegalArgumentException("Class does not appear to be naturally sortable via Comparable: " + classToCompare);
			}

			return createNaturalFallback(serializedForm);
		}

		private static boolean supportsNaturalOrdering(Class<?> classToCompare) {
			if (!Comparable.class.isAssignableFrom(classToCompare)) {
				return false;
			}

			for (Type type : classToCompare.getGenericInterfaces()) {
				if (!(type instanceof ParameterizedType)) {
					continue;
				}

				ParameterizedType pt = (ParameterizedType) type;

				if (Comparable.class != pt.getRawType()) {
					continue;
				}

				Type[] typeArguments = pt.getActualTypeArguments();

				if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class)) {
					continue;
				}

				Class<?> parameterClass = (Class<?>) typeArguments[0];

				return parameterClass.isAssignableFrom(classToCompare);
			}

			return false;
		}

		@SuppressWarnings("unchecked")
		private static <T, C extends Comparable<? super C>> SerializableOrderingComparator<T> createNaturalFallback(
				OrderingComparatorSerializedForm<T> serializedForm) {
			return (SerializableOrderingComparator<T>) Comparators.ordering()
					.serializable()
					.fallbackToNaturalOrdering((Class<C>) serializedForm.getClassToCompare())
					.nullHandling(serializedForm.getNullHandling())
					.hashParameters(serializedForm.getHashParameters())
					.build(ComparatorProviders.loadForSerialization());
		}

	}

	private SerializationSupport() {
		throw new AssertionError("No instances");
	}

}
