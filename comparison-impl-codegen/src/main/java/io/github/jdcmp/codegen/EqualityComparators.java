package io.github.jdcmp.codegen;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.MissingCriteriaException;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode;
import io.github.jdcmp.api.builder.equality.EqualityFallbackMode.FallbackMapper;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.comparator.equality.SerializableEqualityComparator;
import io.github.jdcmp.api.documentation.NotThreadSafe;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.EqualityCriterion;
import io.github.jdcmp.api.getter.SerializableEqualityCriterion;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.api.spec.equality.BaseEqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.EqualityComparatorSpec;
import io.github.jdcmp.api.spec.equality.SerializableEqualityComparatorSpec;
import io.github.jdcmp.codegen.Fallbacks.IdentityFallback;
import io.github.jdcmp.codegen.Fallbacks.SerializableIdentityFallback;
import io.github.jdcmp.codegen.bridge.StaticInitializerBridge;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

@ThreadSafe
final class EqualityComparators {

	public static <T> EqualityComparator<T> create(EqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
		return NonSerializableImpl.create(userSpec, implSpec);
	}

	public static <T> SerializableEqualityComparator<T> createSerializable(SerializableEqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
		return SerializableImpl.create(userSpec, implSpec);
	}

	private static final class NonSerializableImpl {

		public static <T> EqualityComparator<T> create(EqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
			if (userSpec.hasNoGetters()) {
				return useFallback(userSpec);
			} else if (AsmGenerator.supports(userSpec)) {
				return AsmGenerator.generate(userSpec, implSpec);
			}

			return new ComparatorN<>(userSpec);
		}

		private static <T> EqualityComparator<T> useFallback(EqualityComparatorSpec<T> userSpec) {
			EqualityFallbackMode fallbackMode = userSpec.getFallbackMode().orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new FallbackMapper<EqualityComparator<T>>() {
				@Override
				public EqualityComparator<T> onIdentity() {
					return new IdentityFallback<>(userSpec);
				}
			});
		}

	}

	private static final class SerializableImpl {

		static <T> SerializableEqualityComparator<T> create(SerializableEqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
			if (userSpec.hasNoGetters()) {
				return useFallback(userSpec);
			} else if (AsmGenerator.supports(userSpec)) {
				return AsmGenerator.generateSerializable(userSpec, implSpec);
			}

			return new SerializableComparatorN<>(userSpec, implSpec);
		}

		private static <T> SerializableEqualityComparator<T> useFallback(SerializableEqualityComparatorSpec<T> userSpec) {
			EqualityFallbackMode fallbackMode = userSpec.getFallbackMode().orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new FallbackMapper<SerializableEqualityComparator<T>>() {
				@Override
				public SerializableEqualityComparator<T> onIdentity() {
					return new SerializableIdentityFallback<>(userSpec);
				}
			});
		}

	}

	static final class ComparatorN<T> extends AbstractComparator<T> implements EqualityComparator<T> {

		ComparatorN(EqualityComparatorSpec<T> userSpec) {
			super(userSpec);
		}

	}

	static final class SerializableComparatorN<T> extends AbstractComparator<T> implements SerializableEqualityComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableEqualityComparatorSpec<T> userSpec;

		private final transient ImplSpec implSpec;

		SerializableComparatorN(SerializableEqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
			super(userSpec);
			this.userSpec = Objects.requireNonNull(userSpec);
			this.implSpec = Objects.requireNonNull(implSpec);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new SerializationProxyRequiredException();
		}

		private Object writeReplace() throws ObjectStreamException {
			implSpec.getSerializationMode().throwIfPrevented();

			return userSpec.toSerializedForm();
		}

	}

	static abstract class AbstractComparator<T> implements EqualityComparator<T> {

		private final Spec<T, ?> userSpec;

		protected AbstractComparator(Spec<T, ?> userSpec) {
			this.userSpec = Objects.requireNonNull(userSpec);
		}

		@Override
		public final int hash(@Nullable T object) {
			if (object == null) {
				return 0;
			}

			Spec<T, ?> spec = this.userSpec;

			if (spec.useStrictTypes()) {
				spec.getClassToCompare().cast(object);
			}

			HashParameters hashParameters = spec.getHashParameters();
			final int p = hashParameters.multiplier();
			int h = hashParameters.initialValue();

			for (EqualityCriterion<? super T> getter : spec.getGetters()) {
				h = h * p + getter.hash(object);
			}

			return h;
		}

		@Override
		public final boolean areEqual(@Nullable T self, @Nullable Object other) {
			if (self == other) {
				return true;
			} else if (self == null || other == null) {
				return false;
			}

			Spec<T, ?> spec = this.userSpec;
			Class<T> classToCompare = spec.getClassToCompare();

			if (spec.useStrictTypes()) {
				classToCompare.cast(self);
			}

			if (!classToCompare.isInstance(other)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			T o = (T) other;

			for (EqualityCriterion<? super T> getter : spec.getGetters()) {
				if (!getter.areEqual(self, o)) {
					return false;
				}
			}

			return true;
		}

	}

	@NotThreadSafe
	private static final class AsmGenerator<T, C extends EqualityComparator<T>, G extends EqualityCriterion<T>>
			extends AbstractAsmGenerator<T, C, BaseEqualityComparatorSpec<T, G>> {

		private static final int MAX_SUPPORTED_GETTERS = 32;

		private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

		private static final Method SPEC_TO_SERIALIZED_FORM;

		private static final String SPEC_TO_SERIALIZED_FORM_DESCRIPTOR;

		private static final Method STATIC_INITIALIZER_BRIDGE;

		private static final Method STATIC_INITIALIZER_BRIDGE_SERIALIZABLE;

		private static final Type SPEC_NONSERIALIZABLE_TYPE = Type.getType(EqualityComparatorSpec.class);

		private static final Type SPEC_SERIALIZABLE_TYPE = Type.getType(SerializableEqualityComparatorSpec.class);

		private static final Type GETTER_NONSERIALIZABLE_TYPE = Type.getType(EqualityCriterion.class);

		private static final Type GETTER_SERIALIZABLE_TYPE = Type.getType(SerializableEqualityCriterion.class);

		static {
			try {
				SPEC_TO_SERIALIZED_FORM = SerializableEqualityComparatorSpec.class.getDeclaredMethod("toSerializedForm");
				SPEC_TO_SERIALIZED_FORM_DESCRIPTOR = Type.getMethodDescriptor(SPEC_TO_SERIALIZED_FORM);
				STATIC_INITIALIZER_BRIDGE = StaticInitializerBridge.class.getDeclaredMethod("equality", Lookup.class);
				STATIC_INITIALIZER_BRIDGE_SERIALIZABLE = StaticInitializerBridge.class.getDeclaredMethod("equalitySerializable", Lookup.class);
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
		}

		private final Class<?> comparatorType;

		static boolean supports(Spec<?, ?> spec) {
			int getterCount = spec.getGetterCount();

			return getterCount > 0 && getterCount <= AsmGenerator.MAX_SUPPORTED_GETTERS;
		}

		static <T> EqualityComparator<T> generate(EqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
			AsmGenerator<T, EqualityComparator<T>, EqualityCriterion<T>> generator = new AsmGenerator<>(
					userSpec,
					implSpec,
					EqualityComparator.class);

			return generator.createInstance();
		}

		static <T> SerializableEqualityComparator<T> generateSerializable(SerializableEqualityComparatorSpec<T> userSpec, ImplSpec implSpec) {
			AsmGenerator<T, SerializableEqualityComparator<T>, SerializableEqualityCriterion<T>> generator = new AsmGenerator<>(
					userSpec,
					implSpec,
					SerializableEqualityComparator.class);

			return generator.createInstance();
		}

		private AsmGenerator(BaseEqualityComparatorSpec<T, G> userSpec, ImplSpec implSpec, Class<?> comparatorType) {
			super(userSpec, implSpec);
			this.comparatorType = Objects.requireNonNull(comparatorType);
		}

		@Override
		protected String classNamePrefix() {
			return "GeneratedEqualityComparator";
		}

		@Override
		protected int classNameSuffix() {
			return INSTANCE_COUNTER.getAndIncrement();
		}

		@Override
		protected Class<?> classToCompare() {
			return userSpec.getClassToCompare();
		}

		@Override
		protected Type specType() {
			return isSerializable() ? SPEC_SERIALIZABLE_TYPE : SPEC_NONSERIALIZABLE_TYPE;
		}

		@Override
		protected Class<?> comparatorClass() {
			return comparatorType;
		}

		@Override
		protected boolean validate(BaseEqualityComparatorSpec<T, G> userSpec) {
			return supports(userSpec);
		}

		@Override
		protected HashParameters hashParameters() {
			return userSpec.getHashParameters();
		}

		@Override
		protected boolean strictTypes() {
			return userSpec.useStrictTypes();
		}

		@Override
		protected void addCompatibleSerializationMethod(ClassWriter cw, ClassDescription cd) {
			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "writeReplace", "()Ljava/lang/Object;", null, null);
			mv.visitCode();

			mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "spec", consts.specDescriptor);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					consts.specInternalName,
					SPEC_TO_SERIALIZED_FORM.getName(),
					SPEC_TO_SERIALIZED_FORM_DESCRIPTOR,
					true);

			endReturn(mv, ARETURN);
		}

		@Override
		protected void customize(ClassWriter classWriter, ClassDescription classDescription) {
		}

		@Override
		protected Collection<?> getters() {
			return userSpec.getGetters();
		}

		@Override
		protected Type getterType() {
			return isSerializable() ? GETTER_SERIALIZABLE_TYPE : GETTER_NONSERIALIZABLE_TYPE;
		}

		@Override
		protected Method getStaticInitializerBridgeMethod() {
			return isSerializable() ? STATIC_INITIALIZER_BRIDGE_SERIALIZABLE : STATIC_INITIALIZER_BRIDGE;
		}

	}

	private EqualityComparators() {
		throw new AssertionError("No instances");
	}

}
