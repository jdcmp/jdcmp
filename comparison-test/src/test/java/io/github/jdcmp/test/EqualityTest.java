package io.github.jdcmp.test;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.getter.array.IntArrayGetter;
import io.github.jdcmp.api.getter.array.MultidimensionalArrayGetter;
import io.github.jdcmp.api.getter.object.ObjectGetter;
import io.github.jdcmp.api.getter.object.SerializableObjectGetter;
import io.github.jdcmp.api.getter.primitive.IntGetter;
import io.github.jdcmp.api.getter.primitive.SerializableIntGetter;
import io.github.jdcmp.api.provider.ComparatorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

import java.nio.charset.StandardCharsets;

class EqualityTest {

	static final X x_a_1 = new X(42, "test");

	static final X x_a_2 = new X(42, "test");

	static final X x_b_1 = new X(84, "x");

	static final String string1 = new String("test".getBytes(StandardCharsets.UTF_8));

	static final String string2 = new String("test".getBytes(StandardCharsets.UTF_8));

	private void assertFallbackToIdentity(EqualityComparator<String> comparator) {
		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(42, comparator.hash("test"));

		Assertions.assertTrue(comparator.areEqual(null, null));
		Assertions.assertFalse(comparator.areEqual(string1, null));
		Assertions.assertFalse(comparator.areEqual(null, string1));
		Assertions.assertTrue(comparator.areEqual(string1, string1));
		Assertions.assertTrue(comparator.areEqual(string2, string2));
		Assertions.assertFalse(comparator.areEqual(string1, string2));
		Assertions.assertFalse(comparator.areEqual(string2, string1));
	}

	@Nested
	class Fallbacks {

		@ProviderTest
		void identity(ComparatorProvider provider) {
			EqualityComparator<String> comparator = Comparators.equality()
					.nonSerializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			assertFallbackToIdentity(comparator);
		}

	}

	@Nested
	class Serialization {

		@ProviderTest
		void fallback_identity(ComparatorProvider provider) throws Throwable {
			SerializableEqualityComparator<String> serializable = Comparators.equality()
					.serializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			SerializableEqualityComparator<String> comparator = SerializationUtils.copy(serializable);

			assertFallbackToIdentity(comparator);
		}

		@ProviderTest
		void withGetters(ComparatorProvider provider) throws Throwable {
			SerializableEqualityComparator<X> comparator = Comparators.equality()
					.serializable()
					.requireAtLeastOneGetter(X.class)
					.use(SerializableIntGetter.of(X::getA))
					.use(SerializableObjectGetter.of(X::getB))
					.hashParameters(HashParameters.of(17, 37))
					.build(provider);

			SerializableEqualityComparator<X> deserialized = SerializationUtils.copy(comparator);

			Assertions.assertEquals((17 * 37 + 42) * 37 + "test".hashCode(), deserialized.hash(x_a_1));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertFalse(comparator.areEqual(x_a_1, null));
			Assertions.assertFalse(comparator.areEqual(null, x_a_2));
			Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_1));
			Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_2));
			Assertions.assertTrue(comparator.areEqual(x_a_2, x_a_1));
			Assertions.assertFalse(comparator.areEqual(x_a_1, x_b_1));
		}

	}

	@Nested
	class TypeSafety {

		@ProviderTest
		void voidIsForbidden(ComparatorProvider provider) {
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.equality().nonSerializable().fallbackToIdentity(void.class).build(provider));
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.equality().nonSerializable().fallbackToIdentity(Void.class).build(provider));
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.equality()
							.nonSerializable()
							.requireAtLeastOneGetter(void.class)
							.use(ObjectGetter.of(v -> v))
							.build(provider));
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.equality()
							.nonSerializable()
							.requireAtLeastOneGetter(Void.class)
							.use(ObjectGetter.of(v -> v))
							.build(provider));
		}

		@ProviderTest
		void wrongType_withGetters(ComparatorProvider provider) {
			EqualityComparator<X> originalComparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(X.class)
					.use(IntGetter.of(X::getA))
					.use(ObjectGetter.of(X::getB))
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			@SuppressWarnings("unchecked")
			EqualityComparator<Object> comparator = (EqualityComparator<Object>) (EqualityComparator<?>) originalComparator;

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.hash(new Object()));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_1));
			Assertions.assertFalse(comparator.areEqual(x_a_1, x_b_1));
			Assertions.assertFalse(comparator.areEqual(x_a_1, new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual(new Object(), x_a_1));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual(new Object(), null));
		}

		@ProviderTest
		void wrongType_withGetters_lenientTypes(ComparatorProvider provider) {
			EqualityComparator<X> originalComparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(X.class)
					.use(IntGetter.of(X::getA))
					.use(ObjectGetter.of(X::getB))
					.strictTypes(false)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			@SuppressWarnings("unchecked")
			EqualityComparator<Object> comparator = (EqualityComparator<Object>) (EqualityComparator<?>) originalComparator;

			Assertions.assertThrows(ClassCastException.class, () -> comparator.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual(new Object(), x_a_1));
		}

		@ProviderTest
		void wrongType_withoutGetters(ComparatorProvider provider) {
			EqualityComparator<X> originalComparator = Comparators.equality()
					.nonSerializable()
					.fallbackToIdentity(X.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			@SuppressWarnings("unchecked")
			EqualityComparator<Object> comparator = (EqualityComparator<Object>) (EqualityComparator<?>) originalComparator;

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.hash(new Object()));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_1));
			Assertions.assertFalse(comparator.areEqual(x_a_1, x_a_2));
			Assertions.assertFalse(comparator.areEqual(x_a_1, x_b_1));
			Assertions.assertFalse(comparator.areEqual(x_a_1, new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual(new Object(), x_a_1));
		}

		@ProviderTest
		void wrongType_withoutGetters_lenientTypes(ComparatorProvider provider) {
			EqualityComparator<X> originalComparator = Comparators.equality()
					.nonSerializable()
					.fallbackToIdentity(X.class)
					.strictTypes(false)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			@SuppressWarnings("unchecked")
			EqualityComparator<Object> comparator = (EqualityComparator<Object>) (EqualityComparator<?>) originalComparator;

			comparator.hash(new Object());
			comparator.areEqual(new Object(), new Object());
		}

	}

	@ProviderTest
	void hash(ComparatorProvider provider) {
		EqualityComparator<X> comparator = Comparators.equality()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ObjectGetter.of(X::getB))
				.hashParameters(HashParameters.of(17, 37))
				.build(provider);

		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(x_a_1.hashCode(), comparator.hash(x_a_1));
		Assertions.assertNotEquals(x_a_1.hashCode(), comparator.hash(x_b_1));
	}

	@ProviderTest
	void areEqual(ComparatorProvider provider) {
		EqualityComparator<X> comparator = Comparators.equality()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ObjectGetter.of(X::getB))
				.hashParameters(HashParameters.of(42, 84))
				.build(provider);

		Assertions.assertTrue(comparator.areEqual(null, null));
		Assertions.assertFalse(comparator.areEqual(x_a_1, null));
		Assertions.assertFalse(comparator.areEqual(null, x_a_2));
		Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_1));
		Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_2));
		Assertions.assertTrue(comparator.areEqual(x_a_2, x_a_1));
		Assertions.assertFalse(comparator.areEqual(x_a_1, x_b_1));
	}

	@ProviderTest
	void primitive_array(ComparatorProvider provider) {
		EqualityComparator<int[]> comparator = Comparators.equality()
				.nonSerializable()
				.requireAtLeastOneGetter(int[].class)
				.use(IntArrayGetter.of(a -> a))
				.build(provider);

		int[] original = new int[] {1, 2, 3};
		int[] copy = original.clone();
		int[] other = new int[] {1, 2, 4};

		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(comparator.hash(original), comparator.hash(copy));
		Assertions.assertNotEquals(comparator.hash(original), comparator.hash(other));

		Assertions.assertTrue(comparator.areEqual(null, null));
		Assertions.assertFalse(comparator.areEqual(original, null));
		Assertions.assertFalse(comparator.areEqual(null, original));
		Assertions.assertTrue(comparator.areEqual(original, copy));
		Assertions.assertFalse(comparator.areEqual(original, other));
	}

	@ProviderTest
	void primitive_array_multidimensional(ComparatorProvider provider) {
		EqualityComparator<int[][]> comparator = Comparators.equality()
				.nonSerializable()
				.requireAtLeastOneGetter(int[][].class)
				.use(MultidimensionalArrayGetter.of(a -> a))
				.build(provider);

		int[][] original = new int[][] {{1, 2, 3}, {4}};
		int[][] copy = original.clone();
		int[][] other = new int[][] {{1, 2, 3}, {4, 5}};

		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(comparator.hash(original), comparator.hash(copy));
		Assertions.assertNotEquals(comparator.hash(original), comparator.hash(other));

		Assertions.assertTrue(comparator.areEqual(null, null));
		Assertions.assertFalse(comparator.areEqual(original, null));
		Assertions.assertFalse(comparator.areEqual(null, original));
		Assertions.assertTrue(comparator.areEqual(original, copy));
		Assertions.assertFalse(comparator.areEqual(original, other));
	}

}
