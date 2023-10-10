package io.github.jdcmp.codegen;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.getter.primitive.SerializableIntGetter;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;
import io.github.jdcmp.codegen.customization.AvailableClassDefiner;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SerializationBypassProxyTest {

	static final Lookup lookup = MethodHandles.lookup();

	CodegenProvider provider;

	@BeforeEach
	void beforeEach() {
		provider = CodegenProvider.of(lookup);
		// VM-anonymous or hidden classes cannot be used directly, deserialization would throw ClassNotFoundException
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
				SerializableEqualityComparator<String> comparator = createComparator(1);
				byte[] bytes = generateSerializationBytesBypassingProxy(comparator);

				SerializableEqualityComparator<String> deserialized = SerializationUtils.deserialize(bytes);

				Assertions.assertSame(comparator.getClass(), deserialized.getClass());
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
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

			@Test
			public void serialize_getters_0() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(0);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void serialize_getters_1() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(1);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void serialize_getters_n() throws IOException {
				SerializableEqualityComparator<String> comparator = createComparator(64);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

		}

		private void testComparator(int getters) throws IOException {
			SerializableEqualityComparator<String> comparator = createComparator(getters);
			byte[] bytes = generateSerializationBytesBypassingProxy(comparator);

			Exception e = Assertions.assertThrows(Exception.class, () -> SerializationUtils.deserialize(bytes));
			Assertions.assertTrue(e instanceof SerializationProxyRequiredException || e instanceof InvalidClassException);
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
				SerializableOrderingComparator<String> comparator = createComparator(1);
				byte[] bytes = generateSerializationBytesBypassingProxy(comparator);

				SerializableOrderingComparator<String> deserialized = SerializationUtils.deserialize(bytes);

				Assertions.assertSame(comparator.getClass(), deserialized.getClass());
			}

			@Test
			public void getters_n() throws IOException {
				testComparator(64);
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

			@Test
			public void serialize_getters_0() throws IOException {
				SerializableOrderingComparator<String> comparator = createComparator(0);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void serialize_getters_1() throws IOException {
				SerializableOrderingComparator<String> comparator = createComparator(1);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

			@Test
			public void serialize_getters_n() throws IOException {
				SerializableOrderingComparator<String> comparator = createComparator(64);

				Assertions.assertThrows(Exception.class, () -> SerializationUtils.serialize(comparator));
			}

		}

		private void testComparator(int getters) throws IOException {
			SerializableOrderingComparator<String> comparator = createComparator(getters);
			byte[] bytes = generateSerializationBytesBypassingProxy(comparator);

			Exception e = Assertions.assertThrows(Exception.class, () -> SerializationUtils.deserialize(bytes));
			Assertions.assertTrue(e instanceof SerializationProxyRequiredException || e instanceof InvalidClassException);
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

	private static byte[] generateSerializationBytesBypassingProxy(Serializable comparator) throws IOException {
		String name = comparator.getClass().getName();
		long serialVersionUID = ObjectStreamClass.lookup(comparator.getClass()).getSerialVersionUID();

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(new byte[] {-84, -19, 0, 5});
			baos.write(new byte[] {115, 114, 0});
			byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
			baos.write(nameBytes.length);
			baos.write(nameBytes);
			baos.write(longToByteArray(serialVersionUID));
			baos.write(new byte[] {2, 0, 0, 120, 112});

			return baos.toByteArray();
		}
	}

	private static byte[] longToByteArray(long value) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; bytes[i] = (byte) (value >> (7 - i++) * 8)) ;
		return bytes;
	}

}
