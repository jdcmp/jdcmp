package io.github.jdcmp.api.getter.array;

import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.SerializableGenericGetter;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

final class SerializableComparatorGetter<T, R> implements SerializableOrderingCriterion<T> {

	private static final long serialVersionUID = 1L;

	private final transient SerializableGenericGetter<? super T, ? extends R> getter;

	private final transient Comparator<? super R> comparator;

	SerializableComparatorGetter(SerializableGenericGetter<? super T, ? extends R> getter, Comparator<? super R> comparator) {
		if (!(comparator instanceof Serializable)) {
			throw new IllegalArgumentException("Comparator must implement Serializable");
		}

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
		Function<? super T, ? extends R> getter = this.getter;

		return comparator.compare(getter.apply(self), getter.apply(other));
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new SerializationProxyRequiredException();
	}

	private Object writeReplace() throws ObjectStreamException {
		return new SerializedForm<>(this);
	}

	private static final class SerializedForm<T, R> implements Serializable {

		private static final long serialVersionUID = 1L;

		private final SerializableGenericGetter<? super T, ? extends R> getter;

		private final Comparator<? super R> comparator;

		private SerializedForm(SerializableComparatorGetter<T, R> getter) {
			this.getter = getter.getter;
			this.comparator = getter.comparator;
		}

		private Object readResolve() throws ObjectStreamException {
			return new SerializableComparatorGetter<>(getter, comparator);
		}

	}

}
