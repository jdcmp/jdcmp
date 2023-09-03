package io.github.jdcmp.codegen;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.MissingCriteriaException;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode;
import io.github.jdcmp.api.builder.ordering.OrderingFallbackMode.FallbackMapper;
import io.github.jdcmp.api.comparator.ordering.NullHandling;
import io.github.jdcmp.api.comparator.ordering.OrderingComparator;
import io.github.jdcmp.api.comparator.ordering.SerializableOrderingComparator;
import io.github.jdcmp.api.documentation.NotThreadSafe;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.getter.OrderingCriterion;
import io.github.jdcmp.api.getter.SerializableOrderingCriterion;
import io.github.jdcmp.api.spec.ordering.BaseOrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.OrderingComparatorSpec;
import io.github.jdcmp.api.spec.ordering.SerializableOrderingComparatorSpec;
import io.github.jdcmp.codegen.Fallbacks.IdentityOrderFallback;
import io.github.jdcmp.codegen.Fallbacks.NaturalOrderFallback;
import io.github.jdcmp.codegen.Fallbacks.SerializableIdentityOrderFallback;
import io.github.jdcmp.codegen.Fallbacks.SerializableNaturalOrderFallback;
import io.github.jdcmp.codegen.bridge.StaticInitializerBridge;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.ObjectStreamException;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

@ThreadSafe
final class OrderingComparators {

	public static <T> OrderingComparator<T> create(OrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
		return NonSerializableImpl.create(userSpec, implSpec);
	}

	public static <T> SerializableOrderingComparator<T> createSerializable(SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
		return SerializableImpl.create(userSpec, implSpec);
	}

	private static final class NonSerializableImpl {

		public static <T> OrderingComparator<T> create(OrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			if (userSpec.hasNoGetters()) {
				return useFallback(userSpec);
			} else if (AsmGenerator.supports(userSpec)) {
				return AsmGenerator.generate(userSpec, implSpec);
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
			return userSpec.getNullHandling().map(new NullHandling.NullHandlingMapper<OrderingComparator<T>>() {
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
				return AsmGenerator.generateSerializable(userSpec, implSpec);
			}

			return handleNulls(userSpec, implSpec, new SerializableComparatorN<>(userSpec, implSpec));
		}

		private static <T> SerializableOrderingComparator<T> useFallback(SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			OrderingFallbackMode fallbackMode = userSpec.getFallbackMode().orElseThrow(MissingCriteriaException::of);

			return fallbackMode.map(new FallbackMapper<SerializableOrderingComparator<T>>() {
				@Override
				public SerializableOrderingComparator<T> onIdentity() {
					return handleNulls(userSpec, implSpec, new SerializableIdentityOrderFallback<>(userSpec));
				}

				@Override
				public SerializableOrderingComparator<T> onNatural() {
					return handleNulls(userSpec,implSpec, naturalOrderingFallback(userSpec));
				}
			});
		}

		@SuppressWarnings("unchecked")
		private static <T, C extends Comparable<? super C>> SerializableOrderingComparator<T> naturalOrderingFallback(
				SerializableOrderingComparatorSpec<?> spec) {
			SerializableOrderingComparatorSpec<C> cast = (SerializableOrderingComparatorSpec<C>) spec;
			SerializableNaturalOrderFallback<C> fallback = new SerializableNaturalOrderFallback<>(cast);

			return (SerializableOrderingComparator<T>) fallback;
		}

		private static <T> SerializableOrderingComparator<T> handleNulls(
				SerializableOrderingComparatorSpec<T> userSpec,
				ImplSpec implSpec,
				SerializableOrderingComparator<T> comparator) {
			return userSpec.getNullHandling().map(new NullHandling.NullHandlingMapper<SerializableOrderingComparator<T>>() {
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
	static final class ComparatorN<T> extends AbstractComparator<T> {

		ComparatorN(OrderingComparatorSpec<T> userSpec) {
			super(userSpec);
		}

	}

	@ThreadSafe
	static final class SerializableComparatorN<T> extends AbstractComparator<T> implements SerializableOrderingComparator<T> {

		private static final long serialVersionUID = 1L;

		private final transient SerializableOrderingComparatorSpec<T> userSpec;

		private final transient ImplSpec implSpec;

		SerializableComparatorN(SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			super(userSpec);
			this.userSpec = Objects.requireNonNull(userSpec);
			this.implSpec = Objects.requireNonNull(implSpec);
		}

		private Object writeReplace() throws ObjectStreamException {
			implSpec.getSerializationMode().throwIfPrevented();

			return userSpec.toSerializedForm();
		}

	}

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

		private Object writeReplace() throws ObjectStreamException {
			implSpec.getSerializationMode().throwIfPrevented();

			return userSpec.toSerializedForm();
		}

	}

	@NotThreadSafe
	private static final class AsmGenerator<T, C extends OrderingComparator<T>, G extends OrderingCriterion<? super T>> extends AbstractAsmGenerator<T, C, BaseOrderingComparatorSpec<T, G>> {

		private static final int MAX_SUPPORTED_GETTERS = 32;

		private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

		private static final Method SPEC_TO_SERIALIZED_FORM;

		private static final Method STATIC_INITIALIZER_BRIDGE;

		private static final Method STATIC_INITIALIZER_BRIDGE_SERIALIZABLE;

		private static final Type SPEC_NONSERIALIZABLE_TYPE = Type.getType(OrderingComparatorSpec.class);

		private static final Type SPEC_SERIALIZABLE_TYPE = Type.getType(SerializableOrderingComparatorSpec.class);

		private static final Type GETTER_NONSERIALIZABLE_TYPE = Type.getType(OrderingCriterion.class);

		private static final Type GETTER_SERIALIZABLE_TYPE = Type.getType(SerializableOrderingCriterion.class);

		static {
			try {
				SPEC_TO_SERIALIZED_FORM = SerializableOrderingComparatorSpec.class.getDeclaredMethod("toSerializedForm");
				STATIC_INITIALIZER_BRIDGE = StaticInitializerBridge.class.getDeclaredMethod("ordering", Lookup.class);
				STATIC_INITIALIZER_BRIDGE_SERIALIZABLE = StaticInitializerBridge.class.getDeclaredMethod("orderingSerializable", Lookup.class);
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
		}

		private final Class<?> comparatorType;

		public static boolean supports(BaseOrderingComparatorSpec<?, ?> spec) {
			int getterCount = spec.getGetterCount();

			return getterCount > 0 && getterCount <= AsmGenerator.MAX_SUPPORTED_GETTERS;
		}

		static <T> OrderingComparator<T> generate(OrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			AsmGenerator<T, OrderingComparator<T>, OrderingCriterion<T>> generator = new AsmGenerator<>(
					userSpec,
					implSpec,
					OrderingComparator.class);

			return generator.createInstance();
		}

		static <T, C extends SerializableOrderingComparator<T>, G extends SerializableOrderingCriterion<? super T>> SerializableOrderingComparator<T>
		generateSerializable(SerializableOrderingComparatorSpec<T> userSpec, ImplSpec implSpec) {
			AsmGenerator<T, SerializableOrderingComparator<T>, SerializableOrderingCriterion<T>> generator = new AsmGenerator<>(
					userSpec,
					implSpec,
					SerializableOrderingComparator.class);

			return generator.createInstance();
		}

		private AsmGenerator(BaseOrderingComparatorSpec<T, G> userSpec, ImplSpec implSpec, Class<?> comparatorType) {
			super(userSpec, implSpec);
			this.comparatorType = Objects.requireNonNull(comparatorType);
		}

		@Override
		protected String classNamePrefix() {
			return "GeneratedOrderingComparator";
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
		protected Class<?> comparatorClass() {
			return comparatorType;
		}

		@Override
		protected Type specType() {
			return isSerializable() ? SPEC_SERIALIZABLE_TYPE : SPEC_NONSERIALIZABLE_TYPE;
		}

		@Override
		protected boolean validate(BaseOrderingComparatorSpec<T, G> userSpec) {
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
			String descriptor = Type.getMethodDescriptor(SPEC_TO_SERIALIZED_FORM);
			mv.visitMethodInsn(INVOKEINTERFACE, consts.specInternalName, SPEC_TO_SERIALIZED_FORM.getName(), descriptor, true);

			endReturn(mv, ARETURN);
		}

		@Override
		protected void customize(ClassWriter cw, ClassDescription cd) {
			addCompareMethod(cw, cd);
		}

		@Override
		protected Collection<?> getters() {
			return userSpec.getGetters();
		}

		@Override
		protected Type getterType() {
			return isSerializable() ? GETTER_SERIALIZABLE_TYPE : GETTER_NONSERIALIZABLE_TYPE;
		}

		private void addCompareMethod(ClassWriter cw, ClassDescription cd) {
			new CompareTo(cd).addTo(cw);
		}

		@Override
		protected Method getStaticInitializerBridgeMethod() {
			return isSerializable() ? STATIC_INITIALIZER_BRIDGE_SERIALIZABLE : STATIC_INITIALIZER_BRIDGE;
		}

		private final class CompareTo {

			private static final String descriptorNoBridge = "(Ljava/lang/Object;Ljava/lang/Object;)I";

			private final ClassDescription cd;

			private final String descriptorTypeSafe;

			CompareTo(ClassDescription cd) {
				this.cd = Objects.requireNonNull(cd);
				String descriptor = consts.classToCompareDescriptor;
				this.descriptorTypeSafe = "(" + descriptor + descriptor + ")I";
			}

			public void addTo(ClassWriter cw) {
				String descriptor = implSpec.generateBridgeMethods() ? descriptorTypeSafe : descriptorNoBridge;

				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "compare", descriptor, descriptorTypeSafe, null);
				mv.visitCode();

				if (strictTypes()) {
					mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", Consts.CLASS_DESCRIPTOR);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
				}

				addNullHandling(mv);

				String getterInternalName = consts.getterInternalName;
				String getterDescriptor = consts.getterDescriptor;
				int i = 0;
				for (; i < getterCount() - 1; ++i) {
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

				if (implSpec.generateBridgeMethods()) {
					addBridgeMethod(cw);
				}
			}

			private void addNullHandling(MethodVisitor mv) {
				NullHandling nullHandling = userSpec.getNullHandling();

				if (NullHandling.THROW.equals(nullHandling)) {
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "requireNonNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
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
				MethodVisitor mv = cw.visitMethod(Consts.ACCESS_BRIDGE, "compare", "(Ljava/lang/Object;Ljava/lang/Object;)I", null, null);
				mv.visitCode();

				String descriptor = consts.classToCompareDescriptor;
				String internalName = consts.classToCompareInternalName;
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
