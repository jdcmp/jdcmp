package io.github.jdcmp.test;

import java.util.Arrays;

final class Y {

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
