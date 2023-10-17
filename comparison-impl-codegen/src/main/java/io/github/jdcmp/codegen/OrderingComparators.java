package io.github.jdcmp.codegen;

import io.github.jdcmp.api.MissingCriteriaException;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode.FallbackMapper;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.comparator.ordering.NullHandling.NullHandlingMapper;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;
import io.github.jdcmp.api.spec.ordering.BaseOrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import io.github.jdcmp.codegen.Fallbacks.IdentityOrderFallback;
import io.github.jdcmp.codegen.Fallbacks.NaturalOrderFallback;
import io.github.jdcmp.codegen.Fallbacks.SerializableIdentityOrderFallback;
import io.github.jdcmp.codegen.Fallbacks.SerializableNaturalOrderFallback;
import io.github.jdcmp.codegen.bridge.StaticInitializerBridge;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.POP;

@ThreadSafe
final class OrderingComparators {

	public static <T> OrderingComparator<T> create(OrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
		return NonSerializableImpl.create(userSpec, implSpec);
	}

	public static <T> SerializableOrderingComparator<T> createSerializable(
			SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
		return SerializableImpl.create(userSpec, implSpec);
	}

	private static final class NonSerializableImpl {

		public static <T> OrderingComparator<T> create(OrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			if (userSpec.hasNoGetters()) {
				return useFallback(userSpec);
			} else if (AsmGenerator.supports(userSpec)) {
				return AsmGenerator.GENERATOR.generate(userSpec, implSpec);
			}

			return handleNulls(userSpec, new ComparatorN<>(userSpec));
		}

		private static <T> OrderingComparator<T> useFallback(OrderingComparatorSpec<T> userSpec) {
			OrderingFallbackMode fallbackMode = userSpec.getFallbackMode().orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new FallbackMapper<OrderingComparator<T>>() {
				@Override
				public OrderingComparator<T> onIdentity() {
					return handleNulls(userSpec, new IdentityOrderFallback<>(userSpec));
				}

				@Override
				public OrderingComparator<T> onNatural() {
					return handleNulls(userSpec, naturalOrderingFallback(userSpec));
				}
			});
		}

		@SuppressWarnings("unchecked")
		private static <T, C extends Comparable<? super C>> OrderingComparator<T> naturalOrderingFallback(OrderingComparatorSpec<T> spec) {
			OrderingComparatorSpec<C> comparableSpec = (OrderingComparatorSpec<C>) spec;
			NaturalOrderFallback<C> fallback = new NaturalOrderFallback<>(comparableSpec);

			return (OrderingComparator<T>) fallback;
		}

		private static <T> OrderingComparator<T> handleNulls(OrderingComparatorSpec<T> userSpec, OrderingComparator<T> comparator) {
			return userSpec.getNullHandling().map(new NullHandlingMapper<OrderingComparator<T>>() {
				@Override
				public OrderingComparator<T> onThrow() {
					return comparator;
				}

				@Override
				public OrderingComparator<T> onNullsFirst() {
					Comparator<T> nullSafeComparator = Comparator.nullsFirst(comparator);

					return new DelegatingComparator<>(comparator, nullSafeComparator);
				}

				@Override
				public OrderingComparator<T> onNullsLast() {
					Comparator<T> nullSafeComparator = Comparator.nullsLast(comparator);

					return new DelegatingComparator<>(comparator, nullSafeComparator);
				}
			});
		}

	}

	private static final class SerializableImpl {

		public static <T> SerializableOrderingComparator<T> create(SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			if (userSpec.hasNoGetters()) {
				return useFallback(userSpec, implSpec);
			} else if (AsmGenerator.supports(userSpec)) {
				return AsmGenerator.GENERATOR_SERIALIZABLE.generate(userSpec, implSpec);
			}

			SerializableComparatorN<T> nonNullComparator = new SerializableComparatorN<>(userSpec, implSpec.getSerializationMode());

			return handleNulls(userSpec, implSpec, nonNullComparator);
		}

		private static <T> SerializableOrderingComparator<T> useFallback(
				SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			OrderingFallbackMode fallbackMode = userSpec.getFallbackMode().orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new FallbackMapper<SerializableOrderingComparator<T>>() {
				@Override
				public SerializableOrderingComparator<T> onIdentity() {
					return handleNulls(userSpec, implSpec, identityFallback(userSpec, implSpec));
				}

				@Override
				public SerializableOrderingComparator<T> onNatural() {
					return handleNulls(userSpec, implSpec, naturalOrderingFallback(userSpec, implSpec));
				}
			});
		}

		private static <T> SerializableIdentityOrderFallback<T> identityFallback(
				SerializableOrderingComparatorSpec<T> userSpec,
				ImplSpec implSpec) {
			return new SerializableIdentityOrderFallback<>(userSpec, implSpec.getSerializationMode());
		}

		@SuppressWarnings("unchecked")
		private static <T, C extends Comparable<? super C>> SerializableOrderingComparator<T> naturalOrderingFallback(
				SerializableOrderingComparatorSpec<?> userSpec,
				ImplSpec implSpec) {
			SerializableOrderingComparatorSpec<C> cast = (SerializableOrderingComparatorSpec<C>) userSpec;
			SerializableNaturalOrderFallback<C> fallback = new SerializableNaturalOrderFallback<>(cast, implSpec.getSerializationMode());

			return (SerializableOrderingComparator<T>) fallback;
		}

		private static <T> SerializableOrderingComparator<T> handleNulls(
				SerializableOrderingComparatorSpec<T> userSpec,
				ImplSpec implSpec,
				SerializableOrderingComparator<T> comparator) {
			return userSpec.getNullHandling().map(new NullHandlingMapper<SerializableOrderingComparator<T>>() {
				@Override
				public SerializableOrderingComparator<T> onThrow() {
					return comparator;
				}

				@Override
				public SerializableOrderingComparator<T> onNullsFirst() {
					Comparator<T> nullSafeComparator = Comparator.nullsFirst(comparator);

					return new SerializableDelegatingComparator<>(comparator, nullSafeComparator, userSpec, implSpec);
				}

				@Override
				public SerializableOrderingComparator<T> onNullsLast() {
					Comparator<T> nullSafeComparator = Comparator.nullsLast(comparator);

					return new SerializableDelegatingComparator<>(comparator, nullSafeComparator, userSpec, implSpec);
				}
			});
		}

	}

	@ThreadSafe
	private static final class ComparatorN<T> extends AbstractComparator<T> {

		ComparatorN(OrderingComparatorSpec<T> userSpec) {
			super(userSpec);
		}

	}

	@ThreadSafe
	private static final class SerializableComparatorN<T> extends AbstractComparator<T>
			implements SerializableOrderingComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableOrderingComparatorSpec<T> userSpec;

		private final transient AvailableSerializationMode serializationMode;

		SerializableComparatorN(SerializableOrderingComparatorSpec<T> userSpec, AvailableSerializationMode serializationMode) {
			super(userSpec);
			this.userSpec = Objects.requireNonNull(userSpec);
			this.serializationMode = Objects.requireNonNull(serializationMode);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new SerializationProxyRequiredException();
		}

		private Object writeReplace() throws ObjectStreamException {
			serializationMode.throwIfPrevented();

			return userSpec.toSerializedForm();
		}

	}

	@ThreadSafe
	private static abstract class AbstractComparator<T> extends EqualityComparators.AbstractComparator<T>
			implements OrderingComparator<T> {

		private final BaseOrderingComparatorSpec<T, ?> userSpec;

		protected AbstractComparator(BaseOrderingComparatorSpec<T, ?> userSpec) {
			super(userSpec);
			this.userSpec = Objects.requireNonNull(userSpec);
		}

		@Override
		public final int compare(T o1, T o2) {
			BaseOrderingComparatorSpec<T, ?> userSpec = this.userSpec;

			if (userSpec.useStrictTypes()) {
				Class<T> classToCompare = userSpec.getClassToCompare();
				classToCompare.cast(o1);
				classToCompare.cast(o2);
			}

			for (OrderingCriterion<? super T> getter : userSpec.getGetters()) {
				int result = getter.compare(o1, o2);

				if (result != 0) {
					return result;
				}
			}

			return 0;
		}

	}

	private static final class DelegatingComparator<T> implements OrderingComparator<T> {

		private final OrderingComparator<T> delegate;

		private final Comparator<T> comparator;

		private DelegatingComparator(OrderingComparator<T> delegate, Comparator<T> comparator) {
			this.delegate = Objects.requireNonNull(delegate);
			this.comparator = Objects.requireNonNull(comparator);
		}

		@Override
		public int hash(T object) {
			return delegate.hash(object);
		}

		@Override
		public boolean areEqual(T self, Object other) {
			return delegate.areEqual(self, other);
		}

		@Override
		public int compare(T o1, T o2) {
			return comparator.compare(o1, o2);
		}

	}

	@ThreadSafe
	private static final class SerializableDelegatingComparator<T> implements SerializableOrderingComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableOrderingComparator<T> delegate;

		private final transient Comparator<T> comparator;

		private final transient SerializableOrderingComparatorSpec<T> userSpec;

		private final transient ImplSpec implSpec;

		SerializableDelegatingComparator(
				SerializableOrderingComparator<T> delegate,
				Comparator<T> comparator,
				SerializableOrderingComparatorSpec<T> userSpec,
				ImplSpec implSpec) {
			this.delegate = Objects.requireNonNull(delegate);
			this.comparator = Objects.requireNonNull(comparator);
			this.userSpec = Objects.requireNonNull(userSpec);
			this.implSpec = Objects.requireNonNull(implSpec);
		}

		@Override
		public int hash(T object) {
			return delegate.hash(object);
		}

		@Override
		public boolean areEqual(T self, Object other) {
			return delegate.areEqual(self, other);
		}

		@Override
		public int compare(T o1, T o2) {
			return comparator.compare(o1, o2);
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			throw new SerializationProxyRequiredException();
		}

		private Object writeReplace() throws ObjectStreamException {
			implSpec.getSerializationMode().throwIfPrevented();

			return userSpec.toSerializedForm();
		}

	}

	@ThreadSafe
	private static final class AsmGenerator<C extends OrderingComparator<?>>
			extends BytecodeGenerator<C, BaseOrderingComparatorSpec<?, ?>> {

		public static final AsmGenerator<OrderingComparator<?>> GENERATOR;

		public static final AsmGenerator<SerializableOrderingComparator<?>> GENERATOR_SERIALIZABLE;

		private static final int MAX_SUPPORTED_GETTERS = 32;

		private static final Method SPEC_TO_SERIALIZED_FORM;

		static {
			try {
				SPEC_TO_SERIALIZED_FORM = SerializableOrderingComparatorSpec.class.getDeclaredMethod("toSerializedForm");
				Method staticInitializerBridge = StaticInitializerBridge.class.getDeclaredMethod("ordering", Lookup.class);
				Method staticInitializerBridgeSerializable = StaticInitializerBridge.class
						.getDeclaredMethod("orderingSerializable", Lookup.class);

				GeneratorConfig generatorConfig = new GeneratorConfig(
						OrderingComparator.class,
						OrderingComparatorSpec.class,
						OrderingCriterion.class,
						"GeneratedOrderingComparator",
						staticInitializerBridge);
				GeneratorConfig generatorConfigSerializable = new GeneratorConfig(
						SerializableOrderingComparator.class,
						SerializableOrderingComparatorSpec.class,
						SerializableOrderingCriterion.class,
						"GeneratedSerializableOrderingComparator",
						staticInitializerBridgeSerializable);
				GENERATOR = new AsmGenerator<>(generatorConfig);
				GENERATOR_SERIALIZABLE = new AsmGenerator<>(generatorConfigSerializable);
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
		}

		public static boolean supports(BaseOrderingComparatorSpec<?, ?> spec) {
			int getterCount = spec.getGetterCount();

			return getterCount > 0 && getterCount <= AsmGenerator.MAX_SUPPORTED_GETTERS;
		}

		private AsmGenerator(GeneratorConfig config) {
			super(config);
		}

		@Override
		protected void addCompatibleSerializationMethod(ClassWriter cw, ClassDescription cd) {
			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "writeReplace", "()Ljava/lang/Object;", null, null);
			mv.visitCode();

			mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "spec", config.specType.descriptor);
			String descriptor = Type.getMethodDescriptor(SPEC_TO_SERIALIZED_FORM);
			mv.visitMethodInsn(INVOKEINTERFACE, config.specType.internalName, SPEC_TO_SERIALIZED_FORM.getName(), descriptor, true);

			endReturn(mv, ARETURN);
		}

		@Override
		protected void customize(ClassWriter cw, ClassDescription cd, Consts consts) {
			addCompareMethod(cw, cd, consts);
		}

		private void addCompareMethod(ClassWriter cw, ClassDescription cd, Consts consts) {
			new CompareTo(cd, consts).addTo(cw);
		}

		private final class CompareTo {

			private final ClassDescription cd;

			private final Consts consts;

			CompareTo(ClassDescription cd, Consts consts) {
				this.cd = Objects.requireNonNull(cd);
				this.consts = Objects.requireNonNull(consts);
			}

			public void addTo(ClassWriter cw) {
				String classToCompareDescriptor = consts.classToCompare.descriptor;
				String descriptorTypeSafe = "(" + classToCompareDescriptor + classToCompareDescriptor + ")I";
				String descriptor = consts.implSpec.generateBridgeMethods() ? descriptorTypeSafe : "(Ljava/lang/Object;Ljava/lang/Object;)I";

				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "compare", descriptor, descriptorTypeSafe, null);
				mv.visitCode();

				if (consts.userSpec.useStrictTypes()) {
					mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", ClassConsts.CLASS_DESCRIPTOR);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
				}

				addNullHandling(mv);

				String getterInternalName = config.getterType.internalName;
				String getterDescriptor = config.getterType.descriptor;
				int i = 0;
				for (; i < consts.userSpec.getGetterCount() - 1; ++i) {
					mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "getter" + i, getterDescriptor);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEINTERFACE, getterInternalName, "compare", "(Ljava/lang/Object;Ljava/lang/Object;)I", true);
					mv.visitVarInsn(ISTORE, 3);
					mv.visitVarInsn(ILOAD, 3);
					Label label0 = new Label();
					mv.visitJumpInsn(IFEQ, label0);
					mv.visitVarInsn(ILOAD, 3);
					mv.visitInsn(IRETURN);
					mv.visitLabel(label0);
				}

				mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "getter" + i, getterDescriptor);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 2);
				mv.visitMethodInsn(INVOKEINTERFACE, getterInternalName, "compare", "(Ljava/lang/Object;Ljava/lang/Object;)I", true);
				endReturn(mv, IRETURN);

				if (consts.implSpec.generateBridgeMethods()) {
					addBridgeMethod(cw);
				}
			}

			private void addNullHandling(MethodVisitor mv) {
				NullHandling nullHandling = consts.userSpec.getNullHandling();

				if (NullHandling.THROW.equals(nullHandling)) {
					// only fail early if strict types are enabled, otherwise let the criterion handle nulls
					if (consts.userSpec.useStrictTypes()) {
						mv.visitVarInsn(ALOAD, 1);
						mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
						mv.visitInsn(POP);
						mv.visitVarInsn(ALOAD, 2);
						mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
						mv.visitInsn(POP);
					}
					return;
				}

				int leftSideIsNull = nullHandling.getComparisonResultIfLeftSideIsNull();

				mv.visitVarInsn(ALOAD, 1);
				Label label0 = new Label();
				mv.visitJumpInsn(IFNONNULL, label0);
				mv.visitVarInsn(ALOAD, 2);
				Label label1 = new Label();
				mv.visitJumpInsn(IFNONNULL, label1);
				mv.visitInsn(ICONST_0);
				Label label2 = new Label();
				mv.visitJumpInsn(GOTO, label2);
				mv.visitLabel(label1);
				insertNumber(mv, leftSideIsNull);
				mv.visitLabel(label2);
				mv.visitInsn(IRETURN);
				mv.visitLabel(label0);
				mv.visitVarInsn(ALOAD, 2);
				Label label3 = new Label();
				mv.visitJumpInsn(IFNONNULL, label3);
				mv.visitInsn(ICONST_1);
				insertNumber(mv, -leftSideIsNull);
				mv.visitInsn(IRETURN);
				mv.visitLabel(label3);
			}

			private void addBridgeMethod(ClassWriter cw) {
				MethodVisitor mv = cw.visitMethod(ClassConsts.ACCESS_BRIDGE, "compare", "(Ljava/lang/Object;Ljava/lang/Object;)I", null, null);
				mv.visitCode();

				String descriptor = consts.classToCompare.descriptor;
				String internalName = consts.classToCompare.internalName;
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, internalName);
				mv.visitVarInsn(ALOAD, 2);
				mv.visitTypeInsn(CHECKCAST, internalName);
				String targetDescriptor = "(" + descriptor + descriptor + ")I";
				mv.visitMethodInsn(INVOKEVIRTUAL, cd.generatedInternalName, "compare", targetDescriptor, false);

				endReturn(mv, IRETURN);
			}

		}

	}

	private OrderingComparators() {
		throw new AssertionError("No instances");
	}

}
