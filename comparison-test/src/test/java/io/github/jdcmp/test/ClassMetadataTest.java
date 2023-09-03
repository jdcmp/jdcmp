package io.github.jdcmp.test;

import io.github.jdcmp.api.Comparators;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.getter.object.ComparableGetter;
import io.github.jdcmp.api.getter.object.SerializableComparableGetter;
import io.github.jdcmp.api.getter.primitive.IntGetter;
import io.github.jdcmp.api.getter.primitive.SerializableIntGetter;
import io.github.jdcmp.api.provider.ComparatorProvider;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class ClassMetadataTest {

	@ProviderTest
	void signature_class(ComparatorProvider provider) {
		@SuppressWarnings("rawtypes")
		Class<? extends OrderingComparator> clazz = createNonSerializable(provider).getClass();

		Type[] genericInterfaces = clazz.getGenericInterfaces();
		ParameterizedType pt = (ParameterizedType) genericInterfaces[0];

		Assertions.assertEquals(OrderingComparator.class, pt.getRawType());
		Assertions.assertEquals(X.class, pt.getActualTypeArguments()[0]);
	}

	@ProviderTest
	void signature_class_serializable(ComparatorProvider provider) {
		@SuppressWarnings("rawtypes")
		Class<? extends SerializableOrderingComparator> clazz = createSerializable(provider).getClass();

		Type[] genericInterfaces = clazz.getGenericInterfaces();
		ParameterizedType pt = (ParameterizedType) genericInterfaces[0];

		Assertions.assertEquals(SerializableOrderingComparator.class, pt.getRawType());
		Assertions.assertEquals(X.class, pt.getActualTypeArguments()[0]);
	}

	@ProviderTest
	void signature_fields(ComparatorProvider provider) throws Throwable {
		@SuppressWarnings("rawtypes")
		Class<? extends OrderingComparator> clazz = createNonSerializable(provider).getClass();

		ParameterizedType classToCompare = fieldType(clazz, "classToCompare");
		ParameterizedType getter0 = fieldType(clazz, "getter0");
		ParameterizedType getter1 = fieldType(clazz, "getter1");

		Assertions.assertEquals(Class.class, classToCompare.getRawType());
		Assertions.assertEquals(X.class, classToCompare.getActualTypeArguments()[0]);

		Assertions.assertEquals(OrderingCriterion.class, getter0.getRawType());
		Assertions.assertEquals(X.class, getter0.getActualTypeArguments()[0]);

		Assertions.assertEquals(OrderingCriterion.class, getter1.getRawType());
		Assertions.assertEquals(X.class, getter1.getActualTypeArguments()[0]);
	}

	@ProviderTest
	void signature_fields_serializable(ComparatorProvider provider) throws Throwable {
		@SuppressWarnings("rawtypes")
		Class<? extends SerializableOrderingComparator> clazz = createSerializable(provider).getClass();

		ParameterizedType spec = fieldType(clazz, "spec");
		ParameterizedType classToCompare = fieldType(clazz, "classToCompare");
		ParameterizedType getter0 = fieldType(clazz, "getter0");
		ParameterizedType getter1 = fieldType(clazz, "getter1");

		Assertions.assertEquals(SerializableOrderingComparatorSpec.class, spec.getRawType());
		Assertions.assertEquals(X.class, spec.getActualTypeArguments()[0]);

		Assertions.assertEquals(Class.class, classToCompare.getRawType());
		Assertions.assertEquals(X.class, classToCompare.getActualTypeArguments()[0]);

		Assertions.assertEquals(SerializableOrderingCriterion.class, getter0.getRawType());
		Assertions.assertEquals(X.class, getter0.getActualTypeArguments()[0]);

		Assertions.assertEquals(SerializableOrderingCriterion.class, getter1.getRawType());
		Assertions.assertEquals(X.class, getter1.getActualTypeArguments()[0]);
	}

	private ParameterizedType fieldType(Class<?> clazz, String fieldName) throws Exception {
		Field field = clazz.getDeclaredField(fieldName);
		Type genericType = field.getGenericType();

		return (ParameterizedType) genericType;
	}

	private OrderingComparator<X> createNonSerializable(ComparatorProvider provider) {
		return Comparators.ordering()
				.nonSerializable()
				.requireAtLeastOneGetter(X.class)
				.use(IntGetter.of(X::getA))
				.use(ComparableGetter.of(X::getB))
				.build(provider);
	}

	private SerializableOrderingComparator<X> createSerializable(ComparatorProvider provider) {
		return Comparators.ordering()
				.serializable()
				.requireAtLeastOneGetter(X.class)
				.use(SerializableIntGetter.of(X::getA))
				.use(SerializableComparableGetter.of(X::getB))
				.build(provider);
	}


}
