package io.github.jdcmp.test;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.getter.array.ComparableArrayGetter;
import io.github.jdcmp.api.getter.object.ComparableGetter;
import io.github.jdcmp.api.getter.primitive.IntGetter;
import io.github.jdcmp.api.getter.primitive.SerializableIntGetter;
import io.github.jdcmp.api.provider.ComparatorProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

class OrderingTest {

	static final X x_a_1 = new X(42, "test");

	static final X x_a_2 = new X(42, "test");

	static final X x_b_1 = new X(84, "x");

	static final String string1 = new String("test".getBytes(StandardCharsets.UTF_8));

	static final String string2 = new String("test".getBytes(StandardCharsets.UTF_8));

	private void assertFallbackToNaturalOrdering(OrderingComparator<String> comparator) {
		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(42, comparator.hash("test"));

		Assertions.assertTrue(comparator.areEqual(null, null));
		Assertions.assertFalse(comparator.areEqual(string1, null));
		Assertions.assertFalse(comparator.areEqual(null, string1));
		Assertions.assertTrue(comparator.areEqual(string1, string1));
		Assertions.assertTrue(comparator.areEqual(string2, string2));
		Assertions.assertTrue(comparator.areEqual(string1, string2));
		Assertions.assertTrue(comparator.areEqual(string2, string1));

		Assertions.assertEquals(0, comparator.compare(string1, string2));
		Assertions.assertEquals(-1, comparator.compare("1", "2"));
		Assertions.assertEquals(1, comparator.compare("2", "1"));
	}

	private void assertFallbackToIdentity(OrderingComparator<String> comparator) {
		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(42, comparator.hash("test"));

		Assertions.assertTrue(comparator.areEqual(null, null));
		Assertions.assertFalse(comparator.areEqual(string1, null));
		Assertions.assertFalse(comparator.areEqual(null, string1));
		Assertions.assertTrue(comparator.areEqual(string1, string1));
		Assertions.assertTrue(comparator.areEqual(string2, string2));
		Assertions.assertFalse(comparator.areEqual(string1, string2));
		Assertions.assertFalse(comparator.areEqual(string2, string1));

		Assertions.assertEquals(0, comparator.compare("x", "y"));
	}

	private void assertNullsThrow(OrderingComparator<String> comparator) {
		Assertions.assertThrows(NullPointerException.class, () -> comparator.compare(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> comparator.compare(null, ""));
		Assertions.assertThrows(NullPointerException.class, () -> comparator.compare("", null));
	}

	private void assertNullsFirst(OrderingComparator<String> comparator) {
		Assertions.assertEquals(0, comparator.compare(null, null));
		Assertions.assertEquals(-1, comparator.compare(null, ""));
		Assertions.assertEquals(1, comparator.compare("", null));
	}

	private void assertNullsLast(OrderingComparator<String> comparator) {
		Assertions.assertEquals(0, comparator.compare(null, null));
		Assertions.assertEquals(1, comparator.compare(null, ""));
		Assertions.assertEquals(-1, comparator.compare("", null));
	}


	@Nested
	class Fallbacks {

		@ProviderTest
		void empty_fallbackToNaturalOrdering(ComparatorProvider provider) {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToNaturalOrdering(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			assertFallbackToNaturalOrdering(comparator);
			assertNullsThrow(comparator);
		}

		@ProviderTest
		void empty_fallbackToNaturalOrdering_nullsFirst(ComparatorProvider provider) {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToNaturalOrdering(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsFirst()
					.build(provider);

			assertFallbackToNaturalOrdering(comparator);
			assertNullsFirst(comparator);
		}

		@ProviderTest
		void empty_fallbackToNaturalOrdering_nullsLast(ComparatorProvider provider) {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToNaturalOrdering(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsLast()
					.build(provider);

			assertFallbackToNaturalOrdering(comparator);
			assertNullsLast(comparator);
		}

		@ProviderTest
		void empty_requireAtLeastOneGetter(ComparatorProvider provider) {
			Assertions.assertThrows(IllegalArgumentException.class, () -> {
				Comparators.ordering()
						.nonSerializable()
						.requireAtLeastOneGetter(String.class)
						.use(Collections.emptySet())
						.build(provider);
			});
		}

		@ProviderTest
		public void empty_fallbackToIdentity(ComparatorProvider provider) {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			assertFallbackToIdentity(comparator);
		}

		@ProviderTest
		public void empty_fallbackToIdentity_nullsFirst(ComparatorProvider provider) {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsFirst()
					.build(provider);

			assertFallbackToIdentity(comparator);
			assertNullsFirst(comparator);
		}

		@ProviderTest
		public void empty_fallbackToIdentity_nullsLast(ComparatorProvider provider) {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsLast()
					.build(provider);

			assertFallbackToIdentity(comparator);
			assertNullsLast(comparator);
		}

	}

	@Nested
	class Serialization {

		@ProviderTest
		public void serialize_withGetter(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator = Comparators.ordering()
					.serializable()
					.requireAtLeastOneGetter(String.class)
					.use(SerializableIntGetter.of(String::length))
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			OrderingComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			Assertions.assertEquals(42 * 84 + " ".length(), deserialized.hash(" "));
		}

		@ProviderTest
		public void serialize_fallbackIdentity(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator0 = Comparators.ordering()
					.serializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			OrderingComparator<String> comparator = SerializationUtils.deserialize(SerializationUtils.serialize(comparator0));

			assertFallbackToIdentity(comparator);
		}

		@ProviderTest
		public void serialize_fallbackIdentity_nullsFirst(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator = Comparators.ordering()
					.serializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsFirst()
					.build(provider);

			OrderingComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			assertFallbackToIdentity(deserialized);
			assertNullsFirst(deserialized);
		}

		@ProviderTest
		public void serialize_fallbackIdentity_nullsLast(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator = Comparators.ordering()
					.serializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsLast()
					.build(provider);

			OrderingComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			assertFallbackToIdentity(deserialized);
			assertNullsLast(deserialized);
		}

		@ProviderTest
		public void serialize_fallbackNatural(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator0 = Comparators.ordering()
					.serializable()
					.fallbackToNaturalOrdering(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			OrderingComparator<String> comparator = SerializationUtils.deserialize(SerializationUtils.serialize(comparator0));

			assertFallbackToNaturalOrdering(comparator);
			assertNullsThrow(comparator);
		}

		@ProviderTest
		public void serialize_fallbackNatural_nullsFirst(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator = Comparators.ordering()
					.serializable()
					.fallbackToNaturalOrdering(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsFirst()
					.build(provider);

			OrderingComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			assertFallbackToNaturalOrdering(deserialized);
			assertNullsFirst(deserialized);
		}

		@ProviderTest
		public void serialize_fallbackNatural_nullsLast(ComparatorProvider provider) throws Throwable {
			SerializableOrderingComparator<String> comparator = Comparators.ordering()
					.serializable()
					.fallbackToNaturalOrdering(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.nullsLast()
					.build(provider);

			OrderingComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			assertFallbackToNaturalOrdering(deserialized);
			assertNullsLast(deserialized);
		}

	}


	@Nested
	class TypeSafety {

		@ProviderTest
		void voidIsForbidden(ComparatorProvider provider) {
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.ordering().nonSerializable().fallbackToIdentity(void.class).build(provider));
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.ordering().nonSerializable().fallbackToIdentity(Void.class).build(provider));
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.ordering()
							.nonSerializable()
							.requireAtLeastOneGetter(void.class)
							.use(ComparableGetter.of(v -> 1))
							.build(provider));
			Assertions.assertThrows(IllegalArgumentException.class,
					() -> Comparators.ordering()
							.nonSerializable()
							.requireAtLeastOneGetter(Void.class)
							.use(ComparableGetter.of(v -> 1))
							.build(provider));
		}

		@ProviderTest
		void wrongType_withGetters(ComparatorProvider provider) {
			OrderingComparator<Y> originalComparator = Comparators.ordering()
					.nonSerializable()
					.requireAtLeastOneGetter(Y.class)
					.use(ComparableArrayGetter.nullsLast(Y::getA))
					.build(provider);

			@SuppressWarnings("unchecked")
			OrderingComparator<Object> comparator = (OrderingComparator<Object>) (OrderingComparator<?>) originalComparator;

			Assertions.assertThrows(ClassCastException.class, () -> comparator.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual(new Object(), null));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.compare(new Object(), new Object()));
		}

		@ProviderTest
		void wrongType_withGetters_lenientTypes(ComparatorProvider provider) {
			OrderingComparator<Y> originalComparator = Comparators.ordering()
					.nonSerializable()
					.requireAtLeastOneGetter(Y.class)
					.use(ComparableArrayGetter.nullsLast(Y::getA))
					.strictTypes(false)
					.build(provider);

			@SuppressWarnings("unchecked")
			OrderingComparator<Object> comparator = (OrderingComparator<Object>) (OrderingComparator<?>) originalComparator;

			Assertions.assertThrows(ClassCastException.class, () -> comparator.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual("a", new Y()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.compare(new Object(), new Object()));
		}

		@ProviderTest
		void wrongType_withoutGetters(ComparatorProvider provider) {
			OrderingComparator<Y> originalComparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(Y.class)
					.build(provider);

			@SuppressWarnings("unchecked")
			OrderingComparator<Object> comparator = (OrderingComparator<Object>) (OrderingComparator<?>) originalComparator;

			Assertions.assertThrows(ClassCastException.class, () -> comparator.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.areEqual(new Object(), new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> comparator.compare(new Object(), new Object()));
		}

		@ProviderTest
		void wrongType_withoutGetters_lenientTypes(ComparatorProvider provider) {
			OrderingComparator<Y> originalComparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(Y.class)
					.strictTypes(false)
					.build(provider);

			@SuppressWarnings("unchecked")
			OrderingComparator<Object> comparator = (OrderingComparator<Object>) (OrderingComparator<?>) originalComparator;

			comparator.hash(new Object());
			comparator.areEqual(new Object(), new Object());
			comparator.compare(new Object(), new Object());
		}

	}

	@ProviderTest
	void hash(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.fallbackToIdentity(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.hashParameters(HashParameters.of(17, 37))
				.build(provider);

		Assertions.assertEquals(0, comparator.hash(null));
		Assertions.assertEquals(x_a_1.hashCode(), comparator.hash(x_a_1));
		Assertions.assertNotEquals(x_a_1.hashCode(), comparator.hash(x_b_1));
	}

	@ProviderTest
	void areEqual(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.build(provider);

		Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_1));
		Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_2));
		Assertions.assertTrue(comparator.areEqual(x_a_2, x_a_1));
		Assertions.assertFalse(comparator.areEqual(x_a_1, x_b_1));
	}

	@ProviderTest
	void compare(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.build(provider);

		Assertions.assertEquals(0, comparator.compare(x_a_1, x_a_1));
		Assertions.assertEquals(0, comparator.compare(x_a_1, x_a_2));
		Assertions.assertTrue(comparator.compare(x_a_2, x_b_1) < 0);
		Assertions.assertTrue(comparator.compare(x_b_1, x_a_2) > 0);
	}

	@ProviderTest
	void compare_nulls_throw(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.build(provider);

		Assertions.assertThrows(NullPointerException.class, () -> comparator.compare(x_a_1, null));
		Assertions.assertThrows(NullPointerException.class, () -> comparator.compare(null, x_a_1));
		Assertions.assertThrows(NullPointerException.class, () -> comparator.compare(null, null));
	}

	@ProviderTest
	void compare_nulls_first(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.nullsFirst()
				.build(provider);

		Assertions.assertEquals(0, comparator.compare(null, null));
		Assertions.assertTrue(comparator.compare(null, x_a_1) < 0);
		Assertions.assertTrue(comparator.compare(x_a_1, null) > 0);
	}

	@ProviderTest
	void compare_nulls_last(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.nullsLast()
				.build(provider);

		Assertions.assertEquals(0, comparator.compare(null, null));
		Assertions.assertTrue(comparator.compare(null, x_a_1) > 0);
		Assertions.assertTrue(comparator.compare(x_a_1, null) < 0);
	}

	@ProviderTest
	void compare_nullableCriterion_String(ComparatorProvider provider) {
		OrderingComparator<X> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(ComparableGetter.nullsFirst(X::getB))
				.build(provider);

		X x1 = new X(42, null);
		X x2 = new X(43, null);
		X x3 = new X(44, "a");

		Assertions.assertEquals(0, comparator.compare(x1, x2));
		Assertions.assertEquals(0, comparator.compare(x2, x1));
		Assertions.assertTrue(comparator.compare(x1, x3) < 0);
		Assertions.assertTrue(comparator.compare(x3, x2) > 0);
	}

	@ProviderTest
	void compare_nullableCriterion_Array(ComparatorProvider provider) {
		OrderingComparator<Y> comparator = Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(Y.class)
				.use(ComparableArrayGetter.nullsLast(Y::getA))
				.build(provider);

		Y y1 = new Y(new String[] {null});
		Y y2 = new Y(new String[] {null});
		Y y3 = new Y("a");

		Assertions.assertEquals(0, comparator.compare(y1, y2));
		Assertions.assertEquals(0, comparator.compare(y2, y1));
		Assertions.assertTrue(comparator.compare(y1, y3) > 0);
		Assertions.assertTrue(comparator.compare(y3, y2) < 0);
	}

}
