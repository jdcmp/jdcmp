package io.github.jdcmp.test;

import java.util.Objects;

final class X {

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
