package io.github.jdcmp.codegen;


import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import io.github.jdcmp.codegen.Fallbacks.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FallbacksTest {

	@Nested
	class StrictTypes {

		@Test
		public void identityFallback() {
			EqualityComparatorSpec<String> spec = Specs.equality(
					String.class,
					HashParameters.of(17, 37),
					true,
					Collections.emptySet(),
					EqualityFallbackMode.IDENTITY,
					MethodHandles.lookup());
			IdentityFallback fallback = new IdentityFallback<>(spec);

			Assertions.assertThrows(ClassCastException.class, () -> fallback.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.areEqual(new Object(), new Object()));
		}

		@Test
		public void serializableIdentityFallback() throws IOException {
			SerializableEqualityComparatorSpec<String> spec = Specs.equalitySerializable(
					String.class,
					HashParameters.of(17, 37),
					true,
					Collections.emptySet(),
					EqualityFallbackMode.IDENTITY,
					MethodHandles.lookup());
			SerializableIdentityFallback<String> original = new SerializableIdentityFallback<>(spec);
			SerializableIdentityFallback fallback = SerializationUtils.copy(original);

			Assertions.assertThrows(ClassCastException.class, () -> fallback.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.areEqual(new Object(), new Object()));
		}

		@Test
		public void identityOrderFallback() {
			OrderingComparatorSpec<String> spec = Specs.ordering(
					String.class,
					HashParameters.of(17, 37),
					true,
					Collections.emptySet(),
					NullHandling.THROW,
					OrderingFallbackMode.IDENTITY,
					MethodHandles.lookup());
			IdentityOrderFallback fallback = new IdentityOrderFallback<>(spec);

			Assertions.assertThrows(ClassCastException.class, () -> fallback.hash(new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.areEqual(new Object(), new Object()));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.compare(new Object(), ""));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.compare("", new Object()));
		}

		@Test
		public void naturalOrderFallback() {
			OrderingComparatorSpec<String> spec = Specs.ordering(
					String.class,
					HashParameters.of(17, 37),
					true,
					Collections.emptySet(),
					NullHandling.THROW,
					OrderingFallbackMode.NATURAL,
					MethodHandles.lookup());
			NaturalOrderFallback fallback =  new NaturalOrderFallback<>(spec);

			Assertions.assertThrows(ClassCastException.class, () -> fallback.hash(42));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.areEqual(42, null));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.compare(42, null));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.compare(null, 42));
		}

		@Test
		public void serializableNaturalOrderFallback() {
			SerializableOrderingComparatorSpec<String> spec = Specs.orderingSerializable(
					String.class,
					HashParameters.of(17, 37),
					true,
					Collections.emptySet(),
					NullHandling.THROW,
					OrderingFallbackMode.NATURAL,
					MethodHandles.lookup());
			SerializableNaturalOrderFallback fallback = new SerializableNaturalOrderFallback<>(spec);

			Assertions.assertThrows(ClassCastException.class, () -> fallback.hash(42));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.areEqual(42, null));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.compare(42, null));
			Assertions.assertThrows(ClassCastException.class, () -> fallback.compare(null, 42));
		}

	}

	@Nested
	class LenientTypes {

		@Test
		public void identityFallback() {
			EqualityComparatorSpec<String> spec = Specs.equality(
					String.class,
					HashParameters.of(17, 37),
					false,
					Collections.emptySet(),
					EqualityFallbackMode.IDENTITY,
					MethodHandles.lookup());
			IdentityFallback fallback = new IdentityFallback<>(spec);

			Object object = new Object();
			Assertions.assertEquals(17, fallback.hash(object));
			Assertions.assertTrue(fallback.areEqual(object, object));
		}

		@Test
		public void serializableIdentityFallback() {
			SerializableEqualityComparatorSpec<String> spec = Specs.equalitySerializable(
					String.class,
					HashParameters.of(17, 37),
					false,
					Collections.emptySet(),
					EqualityFallbackMode.IDENTITY,
					MethodHandles.lookup());
			SerializableIdentityFallback fallback = new SerializableIdentityFallback<>(spec);

			Object object = new Object();
			Assertions.assertEquals(17, fallback.hash(object));
			Assertions.assertTrue(fallback.areEqual(object, object));
			Assertions.assertFalse(fallback.areEqual(object, new Object()));
		}

		@Test
		public void identityOrderFallback() {
			OrderingComparatorSpec<String> spec = Specs.ordering(
					String.class,
					HashParameters.of(17, 37),
					false,
					Collections.emptySet(),
					NullHandling.THROW,
					OrderingFallbackMode.IDENTITY,
					MethodHandles.lookup());
			IdentityOrderFallback fallback = new IdentityOrderFallback<>(spec);

			Object object = new Object();
			Assertions.assertEquals(17, fallback.hash(object));
			Assertions.assertTrue(fallback.areEqual(object, object));
			Assertions.assertFalse(fallback.areEqual(object, new Object()));
			Assertions.assertEquals(0, fallback.compare("", new Object()));
			Assertions.assertEquals(0, fallback.compare(new Object(), ""));
		}

		@Test
		public void naturalOrderFallback() {
			OrderingComparatorSpec<Integer> spec = Specs.ordering(
					Integer.class,
					HashParameters.of(17, 37),
					false,
					Collections.emptySet(),
					NullHandling.THROW,
					OrderingFallbackMode.NATURAL,
					MethodHandles.lookup());
			NaturalOrderFallback fallback = new NaturalOrderFallback<>(spec);

			String object = new String("test");
			Assertions.assertEquals(17, fallback.hash(object));
			Assertions.assertTrue(fallback.areEqual(object, object));
			Assertions.assertFalse(fallback.areEqual(object, new Object()));
			Assertions.assertEquals(0, fallback.compare("test", object));
			Assertions.assertEquals(0, fallback.compare(object, "test"));
		}

		@Test
		public void serializableNaturalOrderFallback() {
			SerializableOrderingComparatorSpec<Integer> spec = Specs.orderingSerializable(
					Integer.class,
					HashParameters.of(17, 37),
					false,
					Collections.emptySet(),
					NullHandling.THROW,
					OrderingFallbackMode.NATURAL,
					MethodHandles.lookup());
			SerializableNaturalOrderFallback fallback = new SerializableNaturalOrderFallback<>(spec);

			String object = new String("test");
			Assertions.assertEquals(17, fallback.hash(object));
			Assertions.assertTrue(fallback.areEqual(object, object));
			Assertions.assertFalse(fallback.areEqual(object, new Object()));
			Assertions.assertEquals(0, fallback.compare("test", object));
			Assertions.assertEquals(0, fallback.compare(object, "test"));
		}

	}

}
