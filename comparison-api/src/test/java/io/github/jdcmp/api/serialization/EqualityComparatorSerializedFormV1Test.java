package io.github.jdcmp.api.serialization;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.getter.array.SerializableCharArrayGetter;
import io.github.jdcmp.api.spec.Specs;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EqualityComparatorSerializedFormV1Test {

	@Test
	void getters() {
		HashParameters hashParameters = HashParameters.of(17, 37);
		List<SerializableCharArrayGetter<String>> getters = new ArrayList<>();
		getters.add(SerializableCharArrayGetter.of(String::toCharArray));

		SerializableEqualityComparatorSpec<String> spec = Specs.equalitySerializable(
				String.class,
				hashParameters,
				true,
				getters,
				null,
				MethodHandles.lookup());

		EqualityComparatorSerializedFormV1<String> serializedForm = new EqualityComparatorSerializedFormV1<>(spec);

		Assertions.assertEquals(String.class, serializedForm.getClassToCompare());
		Assertions.assertEquals(17, serializedForm.getHashParameters().initialValue());
		Assertions.assertEquals(37, serializedForm.getHashParameters().multiplier());
		Assertions.assertEquals(getters, Arrays.asList(serializedForm.getGetters()));
		Assertions.assertTrue(serializedForm.getStrictTypes());
		Assertions.assertNull(serializedForm.getFallbackMode().orElse(null));
	}

}
