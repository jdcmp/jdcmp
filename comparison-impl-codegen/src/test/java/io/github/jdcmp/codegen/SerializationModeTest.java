package io.github.jdcmp.codegen;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.getter.primitive.SerializableIntGetter;
import io.github.jdcmp.codegen.customization.AvailableClassDefiner;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SerializationModeTest {

	static final Lookup lookup = MethodHandles.lookup();

	CodegenProvider provider;

	@BeforeEach
	void beforeEach() {
		provider = CodegenProvider.of(lookup);
		provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER, AvailableClassDefiner.LOOKUP);
	}

	@Nested
	class Equality {

		@Nested
		class Compatible {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.COMPATIBLE);
			}

			@Test
			public void getters_0() throws IOException {
				testComparator(0);
			}

			@Test
			public void getters_1() throws IOException {
				testComparator(1);
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
			}

		}

		@Nested
		class CompatibleUnprotected {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.COMPATIBLE_UNPROTECTED);
			}

			@Test
			public void getters_0() throws IOException {
				testComparator(0);
			}

			@Test
			public void getters_1() throws IOException {
				testComparator(1);
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
			}

		}

		@Nested
		class Incompatible {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.INCOMPATIBLE);
			}

			@Test
			public void getters_0() throws IOException {
				testComparator(0);
			}

			@Test
			public void getters_1() throws IOException {
				Serializable comparator = createComparator(1);

				Serializable deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

				Assertions.assertSame(comparator.getClass(), deserialized.getClass());
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
			}

			@Test
			public void thatNoSerializationMethodsArePresent() throws Throwable {
				provider.setSerializationMode(AvailableSerializationMode.INCOMPATIBLE);
				provider.setGenerateBridgeMethods(true);

				SerializableEqualityComparator<String> comparator = createComparator(1);

				Class<?> comparatorClass = comparator.getClass();
				Method[] methods = comparatorClass.getDeclaredMethods();

				Method hash = comparatorClass.getDeclaredMethod("hash", String.class);
				Method bridgeHash = comparatorClass.getDeclaredMethod("hash", Object.class);
				Method areEqual = comparatorClass.getDeclaredMethod("areEqual", String.class, Object.class);
				Method bridgeAreEqual = comparatorClass.getDeclaredMethod("areEqual", Object.class, Object.class);

				org.assertj.core.api.Assertions.assertThat(methods)
						.containsOnly(hash, bridgeHash, areEqual, bridgeAreEqual);
			}

		}

		@Nested
		class Hostile {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.HOSTILE);
			}

			@Test
			public void getters_0() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(0);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void getters_1() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(1);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void getters_n() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(64);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

		}

		private void testComparator(int getters) throws IOException {
			SerializableEqualityComparator<String> comparator = createComparator(getters);
			EqualityComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			Assertions.assertEquals(comparator.hash(" "), deserialized.hash(" "));
		}

		private SerializableEqualityComparator<String> createComparator(int getters) {
			return Comparators.equality()
					.serializable()
					.fallbackToIdentity(String.class)
					.use(Stream.generate(() -> SerializableIntGetter.of(String::length)).limit(getters).collect(Collectors.toList()))
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);
		}

	}

	@Nested
	class Ordering {

		@Nested
		class Compatible {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.COMPATIBLE);
			}

			@Test
			public void getters_0() throws IOException {
				testComparator(0);
			}

			@Test
			public void getters_1() throws IOException {
				testComparator(1);
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
			}

		}

		@Nested
		class CompatibleUnprotected {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.COMPATIBLE_UNPROTECTED);
			}

			@Test
			public void getters_0() throws IOException {
				testComparator(0);
			}

			@Test
			public void getters_1() throws IOException {
				testComparator(1);
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
			}

		}

		@Nested
		class Incompatible {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.INCOMPATIBLE);
			}

			@Test
			public void getters_0() throws IOException {
				testComparator(0);
			}

			@Test
			public void getters_1() throws IOException {
				Serializable comparator = createComparator(1);

				Serializable deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

				Assertions.assertSame(comparator.getClass(), deserialized.getClass());
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
			}

			@Test
			public void thatNoSerializationMethodsArePresent() throws Throwable {
				provider.setSerializationMode(AvailableSerializationMode.INCOMPATIBLE);
				provider.setGenerateBridgeMethods(true);

				SerializableOrderingComparator<String> comparator = createComparator(1);

				Class<?> comparatorClass = comparator.getClass();
				Method[] methods = comparatorClass.getDeclaredMethods();

				Method hash = comparatorClass.getDeclaredMethod("hash", String.class);
				Method bridgeHash = comparatorClass.getDeclaredMethod("hash", Object.class);
				Method areEqual = comparatorClass.getDeclaredMethod("areEqual", String.class, Object.class);
				Method bridgeAreEqual = comparatorClass.getDeclaredMethod("areEqual", Object.class, Object.class);
				Method compare = comparatorClass.getDeclaredMethod("compare", String.class, String.class);
				Method bridgeCompare = comparatorClass.getDeclaredMethod("compare", Object.class, Object.class);

				org.assertj.core.api.Assertions.assertThat(methods)
						.containsOnly(hash, bridgeHash, areEqual, bridgeAreEqual, compare, bridgeCompare);
			}

		}

		@Nested
		class Hostile {

			@BeforeEach
			void beforeEach() {
				provider.setSerializationMode(AvailableSerializationMode.HOSTILE);
			}

			@Test
			public void getters_0() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(0);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void getters_1() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(1);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void getters_n() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(64);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

		}

		private void testComparator(int getters) throws IOException {
			SerializableOrderingComparator<String> comparator = createComparator(getters);
			OrderingComparator<String> deserialized = SerializationUtils.deserialize(SerializationUtils.serialize(comparator));

			Assertions.assertEquals(comparator.compare("a", "b"), deserialized.compare("a", "b"));
			Assertions.assertEquals(comparator.compare("a", "bb"), deserialized.compare("a", "bb"));
			Assertions.assertEquals(comparator.compare("aa", "b"), deserialized.compare("aa", "b"));
		}

		private SerializableOrderingComparator<String> createComparator(int getters) {
			return Comparators.ordering()
					.serializable()
					.fallbackToIdentity(String.class)
					.use(Stream.generate(() -> SerializableIntGetter.of(String::length)).limit(getters).collect(Collectors.toList()))
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);
		}

	}

}
