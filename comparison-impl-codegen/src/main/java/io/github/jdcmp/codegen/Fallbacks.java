package io.github.jdcmp.codegen;


import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.util.Objects;

@ThreadSafe
final class Fallbacks {

	@ThreadSafe
	static final class IdentityFallback<T> extends AbstractIdentityFallback<T> implements EqualityComparator<T> {

		IdentityFallback(Spec<?, ?> spec) {
			super(spec);
		}

	}

	@ThreadSafe
	static final class IdentityOrderFallback<T> extends AbstractIdentityOrderFallback<T> implements OrderingComparator<T> {

		IdentityOrderFallback(Spec<?, ?> spec) {
			super(spec);
		}

	}

	@ThreadSafe
	static final class SerializableIdentityFallback<T> extends AbstractIdentityFallback<T> implements SerializableEqualityComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableEqualityComparatorSpec<T> spec;

		SerializableIdentityFallback(SerializableEqualityComparatorSpec<T> spec) {
			super(spec);
			this.spec = Specs.equalitySerializable(spec);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new SerializationProxyRequiredException();
		}

		private Object writeReplace() throws ObjectStreamException {
			return spec.toSerializedForm();
		}

	}

	@ThreadSafe
	static final class SerializableIdentityOrderFallback<T> extends AbstractIdentityOrderFallback<T>
			implements SerializableOrderingComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableOrderingComparatorSpec<T> spec;

		SerializableIdentityOrderFallback(SerializableOrderingComparatorSpec<T> spec) {
			super(spec);
			this.spec = Specs.orderingSerializable(spec);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new SerializationProxyRequiredException();
		}

		private Object writeReplace() throws ObjectStreamException {
			return spec.toSerializedForm();
		}

	}

	@ThreadSafe
	static final class NaturalOrderFallback<T extends Comparable<? super T>> extends AbstractNaturalOrderFallback<T> implements OrderingComparator<T> {

		NaturalOrderFallback(OrderingComparatorSpec<T> spec) {
			super(spec);
		}

	}

	@ThreadSafe
	static final class SerializableNaturalOrderFallback<T extends Comparable<? super T>>
			extends AbstractNaturalOrderFallback<T> implements SerializableOrderingComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableOrderingComparatorSpec<T> spec;

		SerializableNaturalOrderFallback(SerializableOrderingComparatorSpec<T> spec) {
			super(spec);
			this.spec = Specs.orderingSerializable(spec);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new SerializationProxyRequiredException();
		}

		private Object writeReplace() throws ObjectStreamException {
			return spec.toSerializedForm();
		}

	}

	private static abstract class AbstractIdentityFallback<T> implements EqualityComparator<T> {

		protected final Class<?> classToCompare;

		private final int hash;

		protected final boolean strictTypes;

		public AbstractIdentityFallback(Spec<?, ?> spec) {
			this.classToCompare = Objects.requireNonNull(spec.getClassToCompare());
			this.hash = spec.getHashParameters().initialValue();
			this.strictTypes = spec.useStrictTypes();
		}

		@Override
		public final int hash(@Nullable T object) {
			if (object == null) {
				return 0;
			} else if (strictTypes) {
				classToCompare.cast(object);
			}

			return hash;
		}

		@Override
		public final boolean areEqual(@Nullable T self, @Nullable Object other) {
			return (strictTypes ? classToCompare.cast(self) : self) == other;
		}

	}

	private static abstract class AbstractIdentityOrderFallback<T> extends AbstractIdentityFallback<T> implements OrderingComparator<T> {

		protected AbstractIdentityOrderFallback(Spec<?, ?> spec) {
			super(spec);
		}

		@Override
		public final int compare(T self, T other) {
			Objects.requireNonNull(self);
			Objects.requireNonNull(other);

			if (strictTypes) {
				Class<?> classToCompare = this.classToCompare;
				classToCompare.cast(self);
				classToCompare.cast(other);
			}

			return 0;
		}

	}

	private static abstract class AbstractNaturalOrderFallback<T extends Comparable<? super T>> implements OrderingComparator<T> {

		private final Class<T> classToCompare;

		private final int hash;

		private final boolean strictTypes;

		private AbstractNaturalOrderFallback(Spec<T, ?> spec) {
			this.classToCompare = Objects.requireNonNull(spec.getClassToCompare());
			this.hash = spec.getHashParameters().initialValue();
			this.strictTypes = spec.useStrictTypes();
		}

		@Override
		public final int hash(T object) {
			if (object == null) {
				return 0;
			} else if (strictTypes) {
				classToCompare.cast(object);
			}

			return hash;
		}

		@Override
		public final boolean areEqual(T self, Object other) {
			if (self == null) {
				return other == null;
			}

			Class<T> classToCompare = this.classToCompare;

			if ((strictTypes ? classToCompare.cast(self) : self) == other) {
				return true;
			}

			if (classToCompare.isInstance(other)) {
				T o = classToCompare.cast(other);

				return self.compareTo(o) == 0;
			}

			return false;
		}

		@Override
		public final int compare(T self, T other) {
			Class<T> classToCompare = this.classToCompare;

			return strictTypes ? classToCompare.cast(self).compareTo(classToCompare.cast(other)) : self.compareTo(other);
		}

	}

	private Fallbacks() {
		throw new AssertionError("No instances");
	}

}
