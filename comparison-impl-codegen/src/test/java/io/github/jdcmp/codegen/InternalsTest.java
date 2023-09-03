package io.github.jdcmp.codegen;

import io.github.jdcmp.codegen.Internals.PlainFieldFinder;
import io.github.jdcmp.codegen.Internals.PlainFieldSetter;
import io.github.jdcmp.codegen.Internals.TrustedLookupFieldFinder;
import io.github.jdcmp.codegen.Internals.UnsafeFieldSetter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;

public class InternalsTest {

	@Nested
	class FieldFinderTests {

		@Test
		void plain() throws Throwable {
			PlainFieldFinder plain = new PlainFieldFinder();
			Field field = plain.findStatic(String.class, "serialVersionUID");
			Assertions.assertSame(String.class, field.getDeclaringClass());
			Assertions.assertEquals("serialVersionUID", field.getName());
		}

		@Test
		@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_16)
		void trustedLookup() throws Throwable {
			TrustedLookupFieldFinder trustedLookupFieldFinder = new TrustedLookupFieldFinder();
			Field field = trustedLookupFieldFinder.findStatic(System.class, "security");
			Assertions.assertSame(System.class, field.getDeclaringClass());
			Assertions.assertEquals("security", field.getName());
		}

	}

	@Nested
	class FieldSetterTests {

		@Test
		@EnabledOnJre(JRE.JAVA_8)
		void plain() throws Throwable {
			Assertions.assertSame(Integer.class, X.INSTANCE1.getClass());

			PlainFieldSetter plain = new PlainFieldSetter();
			String newValue = "test";
			plain.setStatic(X.class.getDeclaredField("INSTANCE1"), newValue);

			Assertions.assertSame(newValue, X.INSTANCE1);
		}

		@Test
		void unsafe() throws Throwable {
			Assertions.assertSame(Integer.class, X.INSTANCE2.getClass());

			UnsafeFieldSetter unsafe = new UnsafeFieldSetter();
			String newValue = "test";
			unsafe.setStatic(X.class.getDeclaredField("INSTANCE2"), newValue);

			Assertions.assertSame(newValue, X.INSTANCE2);
		}

	}

	@Test
	public void noInstances() {
		Throwable e = Assertions.assertThrows(Throwable.class, () -> {
			Constructor<?> constructor = Internals.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			constructor.newInstance();
		});

		Assertions.assertEquals("No instances", e.getCause().getMessage());
	}

	private static final class X {

		public static final Random RANDOM = new Random();

		private static final Object INSTANCE1 = RANDOM.nextInt();

		private static final Object INSTANCE2 = RANDOM.nextInt();

	}

}
