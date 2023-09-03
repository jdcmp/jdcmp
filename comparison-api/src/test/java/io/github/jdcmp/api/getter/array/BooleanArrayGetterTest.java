package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.SerializationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BooleanArrayGetterTest {

	static final B OBJECT = new B(true, false);

	@Test
	public void of() {
		BooleanArrayGetter<B> getter = BooleanArrayGetter.of(B::getArray);

		Assertions.assertArrayEquals(new boolean[] {true, false}, getter.apply(OBJECT));
	}

	@Test
	public void serializable() throws IOException {
		SerializableBooleanArrayGetter<B> copy = SerializationUtils.copy(SerializableBooleanArrayGetter.of(B::getArray));

		Assertions.assertArrayEquals(new boolean[] {true}, copy.apply(new B(true)));
	}

	@Test
	public void compare_null() {
		B bNull = new B((boolean[]) null);
		BooleanArrayGetter<B> getter = B::getArray;

		Assertions.assertThrows(NullPointerException.class, () -> getter.compare(bNull, bNull));
	}

	@Test
	public void compare_empty() {
		B bEmpty = new B();
		B bTrue = new B(true);
		B bFalse = new B(false);
		BooleanArrayGetter<B> getter = B::getArray;

		Assertions.assertEquals(0, getter.compare(bEmpty, bEmpty));

		Assertions.assertEquals(-1, getter.compare(bEmpty, bTrue));
		Assertions.assertEquals(1, getter.compare(bTrue, bEmpty));

		Assertions.assertEquals(-1, getter.compare(bEmpty, bFalse));
		Assertions.assertEquals(1, getter.compare(bFalse, bEmpty));
	}

	private static final class B {

		private final boolean[] array;

		B(boolean... array) {
			this.array = array;
		}

		public boolean[] getArray() {
			return array;
		}

	}

}
