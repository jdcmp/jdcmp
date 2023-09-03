package io.github.jdcmp.api.serialization;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

final class OrderingComparatorSerializedFormV1<T> implements OrderingComparatorSerializedForm<T> {

	private static final long serialVersionUID = 1L;

	private final Class<T> classToCompare;

	private final int hashInitialValue;

	private final int hashMultiplier;

	private final boolean strictTypes;

	private final SerializableOrderingCriterion<T>[] getters;

	private final NullHandling nullHandling;

	private final @Nullable OrderingFallbackMode fallbackMode;

	OrderingComparatorSerializedFormV1(SerializableOrderingComparatorSpec<T> spec) {
		HashParameters hashParameters = spec.getHashParameters();
		this.classToCompare = Objects.requireNonNull(spec.getClassToCompare());
		this.hashInitialValue = hashParameters.initialValue();
		this.hashMultiplier = hashParameters.multiplier();
		this.strictTypes = spec.useStrictTypes();
		this.getters = toArray(spec.getGetters());
		this.nullHandling = Objects.requireNonNull(spec.getNullHandling());
		this.fallbackMode = spec.getFallbackMode().orElse(null);
	}

	@SuppressWarnings("unchecked")
	private static <T> SerializableOrderingCriterion<T>[] toArray(Iterable<? extends SerializableOrderingCriterion<? super T>> getters) {
		return StreamSupport.stream(getters.spliterator(), false)
				.map(Objects::requireNonNull)
				.toArray(SerializableOrderingCriterion[]::new);
	}

	@Override
	public Class<T> getClassToCompare() {
		return classToCompare;
	}

	@Override
	public HashParameters getHashParameters() {
		return HashParameters.of(hashInitialValue, hashMultiplier);
	}

	@Override
	public boolean getStrictTypes() {
		return strictTypes;
	}

	@Override
	public SerializableOrderingCriterion<T>[] getGetters() {
		return getters.clone();
	}

	@Override
	public NullHandling getNullHandling() {
		return nullHandling;
	}

	@Override
	public Optional<OrderingFallbackMode> getFallbackMode() {
		return Optional.ofNullable(fallbackMode);
	}

	private Object readResolve() throws ObjectStreamException {
		return SerializationSupport.resolve(this);
	}

	@Override
	public String toString() {
		return "OrderingComparatorSerializedFormV1[" +
				"classToCompare=" + classToCompare +
				", hashInitialValue=" + hashInitialValue +
				", hashMultiplier=" + hashMultiplier +
				", strictTypes=" + strictTypes +
				", getters=" + Arrays.toString(getters) +
				", nullHandling=" + nullHandling +
				", fallbackMode=" + fallbackMode +
				"]";
	}

}
