package io.github.jdcmp.codegen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.Collections;

public class UtilsTest {

	@Test
	void chainThrowables_null() {
		Throwable chain = new Throwable();
		Throwable e = new Throwable();

		Assertions.assertThrows(NullPointerException.class, () -> Utils.chainThrowables(null, null));
		Assertions.assertThrows(NullPointerException.class, () -> Utils.chainThrowables(chain, null));
		Assertions.assertSame(e, Utils.chainThrowables(null, e));
	}

	@Test
	void chainThrowables_many() {
		Throwable chain = null;
		Throwable e1 = new Throwable();
		Throwable e2 = new Throwable();
		Throwable e3 = new Throwable();

		chain = Utils.chainThrowables(chain, e1);
		chain = Utils.chainThrowables(chain, e2);
		chain = Utils.chainThrowables(chain, e3);

		Throwable[] suppressed = chain.getSuppressed();

		Assertions.assertSame(e1, chain);
		Assertions.assertSame(e2, suppressed[0]);
		Assertions.assertSame(e3, suppressed[1]);
	}

	@Test
	void isEmpty_array() {
		Assertions.assertTrue(Utils.isEmpty((Object[]) null));
		Assertions.assertTrue(Utils.isEmpty(new Object[0]));
		Assertions.assertFalse(Utils.isEmpty(new Object[] {1}));
	}

	@Test
	void isEmpty_iterable() {
		Assertions.assertTrue(Utils.isEmpty((Iterable<?>) null));
		Assertions.assertTrue(Utils.isEmpty(Collections.emptySet()));
		Assertions.assertFalse(Utils.isEmpty(Collections.singleton(1)));
	}

	@Test
	void or() {
		String x = "x";
		String y = "y";

		Assertions.assertNull(Utils.or(null, null));
		Assertions.assertSame(x, Utils.or(x, null));
		Assertions.assertSame(y, Utils.or(null, y));
		Assertions.assertSame(x, Utils.or(x, y));
		Assertions.assertSame(y, Utils.or(y, x));
	}

	@Test
	void verify() {
		String s = "test";

		Assertions.assertSame(s, Utils.verify(s, v -> !v.isEmpty(), () -> "fail"));
		IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Utils.verify("", v -> !v.isEmpty(), () -> "fail");
		});

		Assertions.assertEquals("fail", iae.getMessage());
	}

	@Test
	public void verifyCanLookupAccess() throws Throwable {
		Lookup lookup = MethodHandles.lookup();
		Class<?> clazz = Class.forName("java.math.BigInteger$UnsafeHolder");
		Utils.verifyLookupCanAccess(lookup, UtilsTest.class);
		Exception e = Assertions.assertThrows(Exception.class, () -> Utils.verifyLookupCanAccess(lookup, clazz));

		Assertions.assertTrue(e instanceof IllegalAccessException || e.getCause() instanceof IllegalAccessException);
	}

	@Test
	public void noInstances() {
		Throwable e = Assertions.assertThrows(Throwable.class, () -> {
			Constructor<?> constructor = Utils.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			constructor.newInstance();
		});

		Assertions.assertEquals("No instances", e.getCause().getMessage());
	}

}
