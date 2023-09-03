package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.object.GenericGetter;

import java.util.Comparator;
import java.util.Objects;

final class ComparatorGetter<T, R> implements OrderingCriterion<T> {

	private final GenericGetter<? super T, ? extends R> getter;

	private final Comparator<? super R> comparator;

	ComparatorGetter(GenericGetter<? super T, ? extends R> getter, Comparator<? super R> comparator) {
		this.getter = Objects.requireNonNull(getter);
		this.comparator = Objects.requireNonNull(comparator);
	}

	@Override
	public int hash(T object) {
		return getter.hash(object);
	}

	@Override
	public boolean areEqual(T self, T other) {
		return getter.areEqual(self, other);
	}

	@Override
	public int compare(T self, T other) {
		GenericGetter<? super T, ? extends R> getter = this.getter;

		return comparator.compare(getter.apply(self), getter.apply(other));
	}

}
