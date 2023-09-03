package io.github.jdcmp.api.getter.array;

public class ByteArrayGetterTest {


	private static final class B {

		private final byte[] array;

		B(byte... array) {
			this.array = array;
		}

		public byte[] getArray() {
			return array;
		}

	}

}
