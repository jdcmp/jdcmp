package io.github.jdcmp.codegen;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.getter.array.CharArrayGetter;
import io.github.jdcmp.api.getter.array.ComparableArrayGetter;
import io.github.jdcmp.api.getter.array.IntArrayGetter;
import io.github.jdcmp.api.getter.object.ComparableGetter;
import io.github.jdcmp.api.getter.object.ObjectGetter;
import io.github.jdcmp.api.getter.object.SerializableComparableGetter;
import io.github.jdcmp.api.getter.primitive.IntGetter;
import io.github.jdcmp.api.getter.primitive.SerializableIntGetter;
import io.github.jdcmp.codegen.contract.EventHandler;
import io.github.jdcmp.codegen.customization.AvailableClassDefiner;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode;
import io.github.jdcmp.codegen.customization.AvailableInstantiator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CodegenProviderTest {

	static final Lookup lookup = MethodHandles.lookup();

	static final X x_a_1 = new X(42, "test");

	static final X x_a_2 = new X(42, "test");

	static final X x_b_1 = new X(84, "x");

	CodegenProvider provider;

	@BeforeEach
	void beforeEach() {
		provider = CodegenProvider.of(lookup);
	}

	@Nested
	class EqualityTests {

		@Test
		void create() {
			EqualityComparator<String> comparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(String.class)
					.use(CharArrayGetter.of(String::toCharArray))
					.build(provider);

			String original = new String("test".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
			String copy = new String(original.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertEquals(comparator.hash(original), comparator.hash(copy));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertFalse(comparator.areEqual(original, null));
			Assertions.assertFalse(comparator.areEqual(null, original));
			Assertions.assertTrue(comparator.areEqual(original, copy));
			Assertions.assertFalse(comparator.areEqual("test", "tesT"));
		}

		@Test
		void create_withoutBridgeMethods() {
			provider.setGenerateBridgeMethods(false);

			EqualityComparator<String> comparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(String.class)
					.use(CharArrayGetter.of(String::toCharArray))
					.build(provider);

			Assertions.assertEquals(2, comparator.getClass().getDeclaredMethods().length);
		}

		@Test
		void create_withBridgeMethods() {
			provider.setGenerateBridgeMethods(true);

			EqualityComparator<String> comparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(String.class)
					.use(CharArrayGetter.of(String::toCharArray))
					.build(provider);

			Assertions.assertEquals(4, comparator.getClass().getDeclaredMethods().length);
		}

		@Test
		void create_primitive_array() {
			EqualityComparator<int[]> comparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(int[].class)
					.use(IntGetter.of(a -> a.length))
					.build(provider);

			int[] original = new int[] {1, 2, 3};
			int[] copy = original.clone();

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertEquals(comparator.hash(original), comparator.hash(copy));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertFalse(comparator.areEqual(original, null));
			Assertions.assertFalse(comparator.areEqual(null, original));
			Assertions.assertTrue(comparator.areEqual(original, copy));
			Assertions.assertTrue(comparator.areEqual(original, new int[] {1, 2, 4}));
			Assertions.assertFalse(comparator.areEqual(original, new int[] {1, 2, 3, 4}));
		}

		@Test
		void create_empty_fallbackToIdentity() {
			EqualityComparator<String> comparator = Comparators.equality()
					.nonSerializable()
					.fallbackToIdentity(String.class)
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			String original = new String("test".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
			String copy = new String(original.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertEquals(42, comparator.hash("test"));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertFalse(comparator.areEqual(original, null));
			Assertions.assertFalse(comparator.areEqual(null, original));
			Assertions.assertTrue(comparator.areEqual(original, original));
			Assertions.assertFalse(comparator.areEqual(original, copy));
			Assertions.assertFalse(comparator.areEqual(copy, original));
		}

		@Test
		void hash() {
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

		@Test
		void areEqual() {
			EqualityComparator<X> comparator = Comparators.equality()
					.nonSerializable()
					.requireAtLeastOneGetter(X.class)
					.use(IntGetter.of(X::getA))
					.use(ObjectGetter.of(X::getB))
					.hashParameters(HashParameters.of(42, 84))
					.build(provider);

			Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_1));
			Assertions.assertTrue(comparator.areEqual(x_a_1, x_a_2));
			Assertions.assertTrue(comparator.areEqual(x_a_2, x_a_1));
			Assertions.assertFalse(comparator.areEqual(x_a_1, x_b_1));
		}

	}

	@Nested
	class OrderingTests {

		@Test
		void create() {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.requireAtLeastOneGetter(String.class)
					.use(CharArrayGetter.of(String::toCharArray))
					.build(provider);

			String test = new String("test".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
			String test2 = new String(test.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
			Assertions.assertEquals(comparator.hash(test), comparator.hash(test2));
			Assertions.assertTrue(comparator.areEqual(test, test2));

			List<String> list = Arrays.asList("bbb", "caa", "aab", "abb", "aaa");
			list.sort(comparator);
			Assertions.assertEquals(Arrays.asList("aaa", "aab", "abb", "bbb", "caa"), list);
		}

		@Test
		void create_primitive_array() {
			OrderingComparator<int[]> comparator = Comparators.ordering()
					.nonSerializable()
					.requireAtLeastOneGetter(int[].class)
					.use(IntArrayGetter.of(a -> a))
					.build(provider);

			int[] original = new int[] {1, 2, 3};
			int[] copy = original.clone();

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertEquals(comparator.hash(original), comparator.hash(copy));

			Assertions.assertTrue(comparator.areEqual(null, null));
			Assertions.assertFalse(comparator.areEqual(original, null));
			Assertions.assertFalse(comparator.areEqual(null, original));
			Assertions.assertTrue(comparator.areEqual(original, copy));
			Assertions.assertTrue(comparator.areEqual(original, new int[] {1, 2, 3}));
			Assertions.assertFalse(comparator.areEqual(original, new int[] {1, 2, 3, 4}));

			Assertions.assertEquals(-1, comparator.compare(new int[] {1, 2}, new int[] {1, 3}));
			Assertions.assertEquals(0, comparator.compare(new int[] {1, 2, 3}, new int[] {1, 2, 3}));
			Assertions.assertEquals(1, comparator.compare(new int[] {1, 3, 3}, new int[] {1, 2, 3}));
		}

		@Test
		void create_withoutBridgeMethods() {
			provider.setGenerateBridgeMethods(false);

			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.requireAtLeastOneGetter(String.class)
					.use(CharArrayGetter.of(String::toCharArray))
					.build(provider);

			Assertions.assertEquals(3, comparator.getClass().getDeclaredMethods().length);
		}

		@Test
		void create_withBridgeMethods() {
			provider.setGenerateBridgeMethods(true);

			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.requireAtLeastOneGetter(String.class)
					.use(CharArrayGetter.of(String::toCharArray))
					.build(provider);

			Assertions.assertEquals(6, comparator.getClass().getDeclaredMethods().length);
		}

		@Test
		void createOrderingComparator_empty_fallbackToNaturalOrdering() {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToNaturalOrdering(String.class)
					.build(provider);

			List<String> list = Arrays.asList("bbb", "caa", "aab", "abb", "aaa");
			list.sort(comparator);
			Assertions.assertEquals(Arrays.asList("aaa", "aab", "abb", "bbb", "caa"), list);
		}

		@Test
		void createOrderingComparator_empty_requireAtLeastOneGetter() {
			Assertions.assertThrows(IllegalArgumentException.class, () -> {
				Comparators.ordering()
						.nonSerializable()
						.requireAtLeastOneGetter(String.class)
						.use(Collections.emptySet())
						.build(provider);
			});
		}

		@Test
		public void createOrderingComparator_empty_noop() {
			OrderingComparator<String> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(String.class)
					.build(provider);

			List<String> list = Arrays.asList("bbb", "caa", "aab", "abb", "aaa");
			List<String> copy = new ArrayList<>(list);
			copy.sort(comparator);
			Assertions.assertEquals(list, copy);
		}

		@Test
		void hash() {
			OrderingComparator<X> comparator = Comparators.ordering()
					.nonSerializable()
					.fallbackToIdentity(X.class)
					.hashParameters(HashParameters.of(17, 37))
					.use(IntGetter.of(X::getA))
					.use(ComparableGetter.of(X::getB))
					.build(provider);

			Assertions.assertEquals(0, comparator.hash(null));
			Assertions.assertEquals(x_a_1.hashCode(), comparator.hash(x_a_1));
			Assertions.assertNotEquals(x_a_1.hashCode(), comparator.hash(x_b_1));
		}

		@Test
		void areEqual() {
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

		@Test
		void compare() {
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

		@Test
		void compare_nulls_throw() {
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

		@Test
		void compare_nulls_first() {
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

		@Test
		void compare_nulls_last() {
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

		@Test
		void compare_nullableCriterion_String() {
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

		@Test
		void compare_nullableCriterion_Array() {
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

	@Nested
	class InstantiatorTests {

		@Test
		void instantiator_default() {
			provider.setInstantiators();
			createComparator();
		}

		@Test
		void instantiator_unsafe() {
			provider.setInstantiators(AvailableInstantiator.UNSAFE);
			Comparator<X> comparator = createComparator();

			org.assertj.core.api.Assertions.assertThat(comparator.getClass().getDeclaredConstructors()).isEmpty();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_16)
		void instantiator_reflectionFactory_classLoader() {
			provider.setInstantiators(AvailableInstantiator.REFLECTION_FACTORY);
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER);
			Comparator<X> comparator = createComparator();

			org.assertj.core.api.Assertions.assertThat(comparator.getClass().getDeclaredConstructors()).isEmpty();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_9)
		void instantiator_reflectionFactory_lookup() {
			provider.setInstantiators(AvailableInstantiator.REFLECTION_FACTORY);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP);
			Comparator<X> comparator = createComparator();

			org.assertj.core.api.Assertions.assertThat(comparator.getClass().getDeclaredConstructors()).isEmpty();
		}

		@Test
		void instantiator_constructor() {
			provider.setInstantiators(AvailableInstantiator.CONSTRUCTOR);
			Comparator<X> comparator = createComparator();

			@SuppressWarnings("rawtypes")
			Class<? extends Comparator> comparatorClass = comparator.getClass();
			org.assertj.core.api.Assertions.assertThat(comparatorClass.getDeclaredConstructors()).isNotEmpty();
		}

	}

	@Nested
	class InitializationModeTests {

		@Test
		@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_16)
		void staticInitializer_classLoader() {
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_16)
		void staticInitializer_vmAnonymous() {
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.VM_ANONYMOUS);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_9)
		void staticInitializer_lookup() {
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_15)
		void staticInitializer_lookupHidden() {
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_16)
		void staticInitializer_lookupHiddenClassData() {
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN_CLASS_DATA);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_16)
		void external_classLoader() {
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_16)
		void external_vmAnonymous() {
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_9)
		void external_lookup() {
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_15)
		void external_lookupHidden() {
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN);
			Assertions.assertThrows(UnsupportedOperationException.class, CodegenProviderTest.this::createComparator);
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_16)
		void external_lookupHiddenClassData() {
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN_CLASS_DATA);
			Assertions.assertThrows(UnsupportedOperationException.class, CodegenProviderTest.this::createComparator);
		}

	}

	@Nested
	class CustomizationTests {

		@Test
		void classDefiner_default() {
			provider.setClassDefiners();
			createComparator();
		}

		@Test
		@EnabledForJreRange(max = JRE.JAVA_16)
		void classDefiner_vmAnonymous() {
			provider.setClassDefiners(AvailableClassDefiner.VM_ANONYMOUS);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_16)
		void classDefiner_lookupHiddenClassData() {
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN_CLASS_DATA);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_15)
		void classDefiner_lookupHidden() {
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN);
			createComparator();
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_9)
		void classDefiner_lookup() {
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP);
			createComparator();
		}

		@Test
		@EnabledForJreRange(max = JRE.JAVA_16)
		void classDefiner_classLoader() {
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER);
			createComparator();
		}

		@Test
		void initializationMode_default() {
			provider.setInitializationMode(null);
			createComparator();
		}

		@Test
		void initializationMode_external() {
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);
			createComparator();
		}

		@Test
		void initializationMode_staticInitializer() {
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			createComparator();
		}

		@Test
		void eventHandler() throws Throwable {
			EventHandler eventHandler = Mockito.mock(EventHandler.class);

			provider.setEventHandler(eventHandler);
			createComparator();
			Mockito.verify(eventHandler).onClassGenerated(Mockito.any(byte[].class));
			Mockito.verify(eventHandler).onClassDefined(Mockito.any(Class.class), Mockito.any(byte[].class));
			Mockito.verify(eventHandler).onClassInstantiated(Mockito.any(Class.class), Mockito.any(byte[].class));
			Mockito.verifyNoMoreInteractions(eventHandler);

			provider.setEventHandler(null);
			createComparator();
			Mockito.verifyNoMoreInteractions(eventHandler);
		}

	}

	private Comparator<X> createComparator() {
		Comparator<X> comparator = Comparators.ordering()
				.serializable()
				.requireAtLeastOneGetter(X.class)
				.use(SerializableIntGetter.of(X::getA))
				.use(SerializableComparableGetter.of(X::getB))
				.nullsLast()
				.build(provider);

		Assertions.assertNotNull(comparator);
		Assertions.assertEquals(-1, comparator.compare(x_a_1, x_b_1));
		Assertions.assertEquals(1, comparator.compare(x_b_1, x_a_1));

		return comparator;
	}

	private static final class X {

		final int a;

		final String b;

		public X(int a, String b) {
			this.a = a;
			this.b = b;
		}

		public int getA() {
			return a;
		}

		public String getB() {
			return b;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof X) {
				X o = (X) obj;

				return this.a == o.a && Objects.equals(this.b, o.b);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return 37 * (37 * 17 + a) + Objects.hashCode(b);
		}

	}

	private static final class Y {

		final String[] a;

		public Y(String... a) {
			this.a = a;
		}

		public String[] getA() {
			return a;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof Y) {
				Y o = (Y) obj;

				return Arrays.equals(this.a, o.a);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(a);
		}

	}

}
