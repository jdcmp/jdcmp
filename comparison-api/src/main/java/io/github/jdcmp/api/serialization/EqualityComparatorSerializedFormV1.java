package io.github.jdcmp.api.serialization;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

final class EqualityComparatorSerializedFormV1<T> implements EqualityComparatorSerializedForm<T> {

	private static final long serialVersionUID = 1L;

	private final Class<T> classToCompare;

	private final int hashInitialValue;

	private final int hashMultiplier;

	private final boolean strictTypes;

	private final SerializableEqualityCriterion<T>[] getters;

	private final @Nullable EqualityFallbackMode fallbackMode;

	EqualityComparatorSerializedFormV1(SerializableEqualityComparatorSpec<T> spec) {
		HashParameters hashParameters = spec.getHashParameters();
		this.classToCompare = Objects.requireNonNull(spec.getClassToCompare());
		this.hashInitialValue = hashParameters.initialValue();
		this.hashMultiplier = hashParameters.multiplier();
		this.strictTypes = spec.useStrictTypes();
		this.getters = toArray(spec.getGetters());
		this.fallbackMode = spec.getFallbackMode().orElse(null);
	}

	@SuppressWarnings("unchecked")
	private static <T> SerializableEqualityCriterion<T>[] toArray(Iterable<? extends SerializableEqualityCriterion<? super T>> getters) {
		return StreamSupport.stream(getters.spliterator(), false)
				.map(Objects::requireNonNull)
				.toArray(SerializableEqualityCriterion[]::new);
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
	public SerializableEqualityCriterion<T>[] getGetters() {
		return getters;
	}

	@Override
	public Optional<EqualityFallbackMode> getFallbackMode() {
		return Optional.ofNullable(fallbackMode);
	}

	private Object readResolve() {
		return SerializationSupport.resolve(this);
	}

	@Override
	public String toString() {
		return "EqualityComparatorSerializedFormV1[" +
				"classToCompare=" + classToCompare +
				", hashInitialValue=" + hashInitialValue +
				", hashMultiplier=" + hashMultiplier +
				", getters=" + Arrays.toString(getters) +
				", fallbackMode=" + fallbackMode +
				", strictTypes=" + strictTypes +
				"]";
	}

}
