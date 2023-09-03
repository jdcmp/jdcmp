package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.OrderingCriterion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

public class ComparatorGetterTest {

	private boolean[] b1;

	private static final class B {

		private final boolean[] array;

		B(boolean... array) {
			this.array = array;
		}

		public boolean[] getArray() {
			return array;
		}

	}

	@Test
	public void compare_nullsFirst() {
		B bNull = new B((boolean[]) null);
		B bTrue = new B(true);
		B bFalse = new B(false);
		// java8 type inference does not allow a lambda method reference here
		Comparator<boolean[]> comparator = Comparator.nullsFirst((a, b) -> BooleanArrayGetter.compare(a, b));
		OrderingCriterion<B> getter = new ComparatorGetter<>(B::getArray, comparator);

		Assertions.assertEquals(0, getter.compare(bNull, bNull));

		Assertions.assertEquals(-1, getter.compare(bNull, bTrue));
		Assertions.assertEquals(1, getter.compare(bTrue, bNull));

		Assertions.assertEquals(-1, getter.compare(bNull, bFalse));
		Assertions.assertEquals(1, getter.compare(bFalse, bNull));
	}

	@Test
	public void compare_nullsLast() {
		B bNull = new B((boolean[]) null);
		B bTrue = new B(true);
		B bFalse = new B(false);
		// java8 type inference does not allow a lambda method reference here
		Comparator<boolean[]> comparator = Comparator.nullsLast((a, b) -> BooleanArrayGetter.compare(a, b));
		OrderingCriterion<B> getter = new ComparatorGetter<>(B::getArray, comparator);

		Assertions.assertEquals(0, getter.compare(bNull, bNull));

		Assertions.assertEquals(1, getter.compare(bNull, bTrue));
		Assertions.assertEquals(-1, getter.compare(bTrue, bNull));

		Assertions.assertEquals(1, getter.compare(bNull, bFalse));
		Assertions.assertEquals(-1, getter.compare(bFalse, bNull));
	}

}
