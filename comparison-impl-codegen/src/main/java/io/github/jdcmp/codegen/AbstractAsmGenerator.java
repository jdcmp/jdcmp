package io.github.jdcmp.codegen;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.documentation.NotThreadSafe;
import io.github.jdcmp.api.serialization.SerializationDisabledException;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.codegen.ClassDefiner.ClassDefinition;
import io.github.jdcmp.codegen.bridge.StaticInitializerBridge;
import io.github.jdcmp.codegen.contract.EventHandler;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import static org.objectweb.asm.Opcodes.*;

/**
 * Main area for bytecode generation.
 *
 * @param <T> Type whose instances can be compared
 * @param <C> Type of the generated comparator
 * @param <U> Type of the user spec
 */
@NotThreadSafe
abstract class AbstractAsmGenerator<T, C, U extends Spec<?, ?>> {

	protected final U userSpec;

	protected final ImplSpec implSpec;

	private final EventHandler eventHandler;

	protected Consts consts;

	protected AbstractAsmGenerator(U userSpec, ImplSpec implSpec) {
		if (!validate(userSpec)) {
			throw new IllegalArgumentException("Unsupported user specification");
		}

		this.userSpec = userSpec;
		this.implSpec = Objects.requireNonNull(implSpec);
		this.eventHandler = Objects.requireNonNull(implSpec.getEventHandler());
	}

	abstract protected String classNamePrefix();

	abstract protected int classNameSuffix();

	abstract protected Class<?> classToCompare();

	abstract protected Class<?> comparatorClass();

	abstract protected Type specType();

	abstract protected boolean validate(U userSpec);

	abstract protected HashParameters hashParameters();

	abstract protected Collection<?> getters();

	abstract protected boolean strictTypes();

	abstract protected Type getterType();

	abstract protected void addCompatibleSerializationMethod(ClassWriter classWriter, ClassDescription classDescription);

	abstract protected void customize(ClassWriter classWriter, ClassDescription classDescription);

	abstract protected Method getStaticInitializerBridgeMethod();

	public final C createInstance() {
		this.consts = new Consts(this);
		List<Callable<C>> strategies = createStrategies();

		if (strategies.isEmpty()) {
			throw new IllegalStateException("Cannot generate class, no strategies are available.");
		}

		return FallbackStrategy.of(strategies).apply(Callable::call);
	}

	private List<Callable<C>> createStrategies() {
		ArrayList<Callable<C>> strategies = new ArrayList<>(16);
		addVmAnonymousStrategies(strategies);
		addLookupHiddenWithClassDataStrategies(strategies);
		addStaticResolverStrategies(strategies);

		return strategies;
	}

	private void addVmAnonymousStrategies(ArrayList<Callable<C>> list) {
		implSpec.getClassDefiners().getVmAnonymousClassDefiner().ifPresent(classDefiner -> {
			for (Instantiator instantiator : implSpec.getInstantiators()) {
				if (instantiator.supportsVmAnonOrHiddenClasses()) {
					list.add(() -> generate(new VmAnonymousStrategy(classDefiner, instantiator)));
				}
			}
		});
	}

	private void addLookupHiddenWithClassDataStrategies(ArrayList<Callable<C>> list) {
		if (AvailableInitializationMode.EXTERNAL.equals(implSpec.getInitializationMode())) {
			return;
		}

		implSpec.getClassDefiners().getLookupHiddenClassWithClassDataDefiner().ifPresent(classDefiner -> {
			for (Instantiator instantiator : implSpec.getInstantiators()) {
				if (instantiator.supportsVmAnonOrHiddenClasses()) {
					list.add(() -> generate(new LookupHiddenWithClassDataStrategy(classDefiner, instantiator)));
				}
			}
		});
	}

	private void addStaticResolverStrategies(ArrayList<Callable<C>> list) {
		for (ClassDefiner classDefiner : implSpec.getClassDefiners().all()) {
			for (Instantiator instantiator : implSpec.getInstantiators()) {
				list.add(() -> generate(new StaticResolverStrategy(classDefiner, instantiator)));
			}
		}
	}

	private C generate(Genesis genesis) {
		byte[] classBytes = genesis.generate();
		Class<C> generatedClass = genesis.define(classBytes);
		C instance = genesis.createInstance(generatedClass);
		handleEvents(generatedClass, classBytes);

		return instance;
	}

	private void handleEvents(Class<C> generatedClass, byte[] classBytes) {
		try {
			eventHandler.onClassInstantiated(generatedClass, classBytes);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected int getterCount() {
		return getters().size();
	}

	protected boolean isSerializable() {
		return Serializable.class.isAssignableFrom(comparatorClass());
	}

	protected static void insertNumber(MethodVisitor mv, int value) {
		if (value >= -1 && value <= 5) {
			mv.visitInsn(ICONST_0 + value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			mv.visitIntInsn(BIPUSH, value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(SIPUSH, value);
		} else {
			mv.visitLdcInsn(value);
		}
	}

	protected static void endReturn(MethodVisitor mv, int returnOpcode) {
		mv.visitInsn(returnOpcode);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private abstract class Genesis {

		protected final ClassDescription cd = new ClassDescription();

		protected final Instantiator instantiator;

		protected Genesis(Instantiator instantiator) {
			this.instantiator = Objects.requireNonNull(instantiator);
		}

		abstract protected Class<C> specificDefineClass(byte[] bytes);

		abstract protected int classVersion();

		abstract protected void visitStaticInitializerLoadSpec(MethodVisitor mv);

		public C createInstance(Class<C> generatedClass) {
			return instantiator.newInstance(generatedClass);
		}

		public Class<C> define(byte[] bytes) {
			Class<C> generatedClass = specificDefineClass(bytes);

			if (AvailableInitializationMode.EXTERNAL.equals(implSpec.getInitializationMode())) {
				initializeFieldsExternally(generatedClass);
			}

			return generatedClass;
		}

		private void initializeFieldsExternally(Class<C> generatedClass) {
			ExternalFieldInitializer fieldInitializer = new ExternalFieldInitializer(generatedClass);

			if (isSerializable()) {
				fieldInitializer.addSpec(userSpec);
			}

			fieldInitializer.addClassToCompare(classToCompare());
			fieldInitializer.addGetters(getters());
		}

		public final byte[] generate() {
			ClassWriter cw = createClassWriter();
			addFields(cw);
			addStaticInitializerIfNecessary(cw);
			addConstructorIfNecessary(cw);
			addInterfaceImpl(cw);
			customize(cw, cd);

			return end(cw);
		}

		private byte[] end(ClassWriter cw) {
			cw.visitEnd();

			return cw.toByteArray();
		}

		protected ClassWriter createClassWriter() {
			String[] interfaces = consts.interfaces;
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			cw.visit(classVersion(), Consts.ACCESS_CLASS, cd.generatedInternalName, cd.generatedSignature, "java/lang/Object", interfaces);

			return cw;
		}

		private void addFields(ClassWriter cw) {
			int access = Consts.ACCESS_FIELD;

			if (isSerializable()) {
				cw.visitField(access, "spec", consts.specDescriptor, consts.specSignature, null).visitEnd();
			}

			String classToCompareSignature = consts.classToCompareSignature;
			cw.visitField(access, "classToCompare", "Ljava/lang/Class;", classToCompareSignature, null).visitEnd();

			String getterDescriptor = consts.getterDescriptor;
			String getterSignature = consts.getterSignature;
			for (int i = 0; i < getterCount(); ++i) {
				cw.visitField(access, "getter" + i, getterDescriptor, getterSignature, null).visitEnd();
			}
		}

		private void addStaticInitializerIfNecessary(ClassWriter cw) {
			implSpec.getInitializationMode().map(new AvailableInitializationMode.InitializationModeMapper<Void>() {
				@Override
				public Void onStaticInitializer() {
					addStaticInitializer(cw);
					return null;
				}

				@Override
				public Void onExternal() {
					return null;
				}
			});
		}

		protected final void addStaticInitializer(ClassWriter cw) {
			MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();

			visitStaticInitializerLoadSpec(mv);
			visitStaticInitializationFromSpec(mv);

			endReturn(mv, RETURN);
		}

		private void visitStaticInitializationFromSpec(MethodVisitor mv) {
			FromSpecFieldInitializer init = new FromSpecFieldInitializer(mv, cd.generatedInternalName, consts.specType, consts);

			if (isSerializable()) {
				mv.visitInsn(DUP);
				init.addSpec(userSpec);
			}

			mv.visitInsn(DUP);
			init.addClassToCompare(classToCompare());
			init.addGetters(getters());
		}

		private void addConstructorIfNecessary(ClassWriter cw) {
			if (!instantiator.requiresConstructor()) {
				return;
			}

			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			endReturn(mv, RETURN);
		}

		private void addInterfaceImpl(ClassWriter cw) {
			new Hash(cd).addTo(cw);
			new AreEqual(cd).addTo(cw);

			if (isSerializable()) {
				new Serialization(cd).addTo(cw);
			}
		}

	}

	private final class VmAnonymousStrategy extends Genesis {

		private final ClassDefiners.VMAnonymousClassDefiner classDefiner;

		private int constantPoolIndex;

		VmAnonymousStrategy(ClassDefiners.VMAnonymousClassDefiner classDefiner, Instantiator instantiator) {
			super(instantiator);
			this.classDefiner = Objects.requireNonNull(classDefiner);
		}

		@Override
		protected int classVersion() {
			return V1_8;
		}

		@Override
		protected ClassWriter createClassWriter() {
			ClassWriter cw = super.createClassWriter();
			constantPoolIndex = cw.newConst("PLACEHOLDER_SPEC");

			return cw;
		}

		@Override
		protected void visitStaticInitializerLoadSpec(MethodVisitor mv) {
			mv.visitLdcInsn("PLACEHOLDER_SPEC");
			mv.visitTypeInsn(CHECKCAST, consts.specInternalName);
		}

		@Override
		protected Class<C> specificDefineClass(byte[] bytes) {
			Lookup host = implSpec.getLookup();
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, host);
			Object[] constantPoolPatches = createConstantPoolPatches(bytes);

			return classDefiner.defineClass(classDefinition, constantPoolPatches);
		}

		private Object[] createConstantPoolPatches(byte[] bytes) {
			int cpi = constantPoolIndex;
			Object[] constantPoolPatches = new Object[cpi + 1];
			for (int i = 0; i < getterCount(); ++i) {
				constantPoolPatches[cpi] = new Object();
			}

			return constantPoolPatches;
		}

	}

	private final class LookupHiddenWithClassDataStrategy extends Genesis {

		final ClassDefiners.LookupHiddenClassWithClassDataDefiner classDefiner;

		LookupHiddenWithClassDataStrategy(ClassDefiners.LookupHiddenClassWithClassDataDefiner classDefiner, Instantiator instantiator) {
			super(instantiator);
			this.classDefiner = Objects.requireNonNull(classDefiner);
		}

		@Override
		protected int classVersion() {
			return V16;
		}

		@Override
		protected void visitStaticInitializerLoadSpec(MethodVisitor mv) {
			Handle handle = LookupHiddenHolder.CLASS_DATA_HANDLE;

			mv.visitLdcInsn(new ConstantDynamic("_", consts.specDescriptor, handle));
		}

		@Override
		protected Class<C> specificDefineClass(byte[] bytes) {
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, implSpec.getLookup());

			return classDefiner.defineClass(classDefinition, userSpec);
		}

	}

	private static final class LookupHiddenHolder {

		static final Handle CLASS_DATA_HANDLE;

		static final ConstantDynamic CLASS_DATA_CONSTANT_DYNAMIC;

		static final String CLASS_DATA_DESCRIPTOR;

		static {
			try {
				Method classData = MethodHandles.class.getDeclaredMethod("classData", Lookup.class, String.class, Class.class);
				String owner = Type.getInternalName(MethodHandles.class);
				String descriptor = Type.getMethodDescriptor(classData);
				CLASS_DATA_DESCRIPTOR = descriptor;
				CLASS_DATA_HANDLE = new Handle(H_INVOKESTATIC, owner, "classData", descriptor, false);
				CLASS_DATA_CONSTANT_DYNAMIC = null;
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
		}

	}

	private final class StaticResolverStrategy extends Genesis {

		private final ClassDefiner classDefiner;

		private final Method bridgeMethod = getStaticInitializerBridgeMethod();

		StaticResolverStrategy(ClassDefiner classDefiner, Instantiator instantiator) {
			super(instantiator);
			this.classDefiner = Objects.requireNonNull(classDefiner);
		}

		@Override
		protected int classVersion() {
			return V1_8;
		}

		@Override
		protected void visitStaticInitializerLoadSpec(MethodVisitor mv) {
			mv.visitMethodInsn(
					INVOKESTATIC,
					"java/lang/invoke/MethodHandles",
					"lookup",
					"()Ljava/lang/invoke/MethodHandles$Lookup;",
					false);
			mv.visitMethodInsn(
					INVOKESTATIC,
					Consts.STATIC_INITIALIZER_BRIDGE_INTERNAL_NAME,
					bridgeMethod.getName(),
					Type.getMethodDescriptor(bridgeMethod),
					false);
		}

		@Override
		protected Class<C> specificDefineClass(byte[] bytes) {
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, implSpec.getLookup());

			return classDefiner.defineClass(classDefinition);
		}

		@Override
		public C createInstance(Class<C> generatedClass) {
			return StaticInitializerBridge.run(generatedClass, userSpec, () -> instantiator.newInstance(generatedClass));
		}

	}

	private final class Serialization {

		private final ClassDescription cd;

		Serialization(ClassDescription cd) {
			this.cd = Objects.requireNonNull(cd);
		}

		void addTo(ClassWriter cw) {
			implSpec.getSerializationMode().map(new AvailableSerializationMode.SerializationModeMapper<Void>() {
				@Override
				public Void onCompatible() {
					addCompatibleSerializationMethod(cw, cd);
					return null;
				}

				@Override
				public Void onIncompatible() {
					return null; // do nothing, care neither about supporting nor preventing serialization
				}

				@Override
				public Void onHostile() {
					addHostileSerializationMethods(cw);
					return null;
				}
			});
		}

		private void addHostileSerializationMethods(ClassWriter cw) {
			addHostileReadObject(cw);
			addHostileWriteObject(cw);
		}

		private void addHostileReadObject(ClassWriter classWriter) {
			String exceptionName = Consts.READ_OBJECT_EXCEPTION_NAME;
			MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "readObject",
					"(Ljava/io/ObjectInputStream;)V", null, Consts.READ_OBJECT_EXCEPTIONS);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, exceptionName);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, exceptionName, "<init>", "()V", false);
			endReturn(mv, ATHROW);
		}

		private void addHostileWriteObject(ClassWriter cw) {
			String exceptionName = Consts.WRITE_OBJECT_EXCEPTION_NAME;
			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "writeObject",
					"(Ljava/io/ObjectOutputStream;)V", null, Consts.WRITE_OBJECT_EXCEPTIONS);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, exceptionName);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, exceptionName, "<init>", "()V", false);
			endReturn(mv, ATHROW);
		}

	}

	private final class Hash {

		final String descriptorNoBridge = "(Ljava/lang/Object;)I";

		final ClassDescription cd;

		final String descriptorBridge;

		final int getterCount = getterCount();

		final HashParameters hashParameters = hashParameters();

		final int hashMultiplier = hashParameters.multiplier();

		Hash(ClassDescription cd) {
			this.cd = cd;
			this.descriptorBridge = "(" + consts.classToCompareDescriptor + ")I";
		}

		private void addTo(ClassWriter cw) {
			final String descriptor = implSpec.generateBridgeMethods() ? descriptorBridge : descriptorNoBridge;
			final String signature = "(" + consts.classToCompareDescriptor + ")I";
			final String getterInternalName = getterType().getInternalName();
			final String getterDescriptor = getterType().getDescriptor();
			final String hashDescriptor = descriptorNoBridge;

			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hash", descriptor, signature, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 1);
			Label label0 = new Label();
			mv.visitJumpInsn(IFNONNULL, label0);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);
			mv.visitLabel(label0);

			int[] multipliers = calculateMultipliers();
			insertNumber(mv, hashParameters.initialValue() * multipliers[0]);

			for (int i = 0; i < getterCount; ++i) {
				mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "getter" + i, getterDescriptor);

				if (strictTypes() && i == 0) {
					mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", Consts.CLASS_DESCRIPTOR);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
				} else {
					mv.visitVarInsn(ALOAD, 1);
				}

				mv.visitMethodInsn(INVOKEINTERFACE, getterInternalName, "hash", hashDescriptor, true);
				if (i < getterCount - 1) {
					insertNumber(mv, multipliers[i + 1]);
					mv.visitInsn(IMUL);
				}
				mv.visitInsn(IADD);
			}

			endReturn(mv, IRETURN);

			if (implSpec.generateBridgeMethods()) {
				addBridgeMethod(cw);
			}
		}

		private void addBridgeMethod(ClassWriter cw) {
			MethodVisitor mv = cw.visitMethod(Consts.ACCESS_BRIDGE, "hash", "(Ljava/lang/Object;)I", null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, consts.classToCompareInternalName);
			String targetDescriptor = "(" + consts.classToCompareDescriptor + ")I";
			mv.visitMethodInsn(INVOKEVIRTUAL, cd.generatedInternalName, "hash", targetDescriptor, false);

			endReturn(mv, IRETURN);
		}

		private int[] calculateMultipliers() {
			/*
				Pre-calculate the {hashInitialValue * hashMultiplier * ...} chain for each level

				h(n) = h(n - 1) * hashMultiplier + object.hashCode()
				h(0) = hashInitialValue * hashMultiplier + a.hashCode()
				h(1) = h(0) * hashMultiplier + b.hashCode()
					 = (hashInitialValue * hashMultiplier + a.hashCode()) * hashMultiplier + b.hashCode()
					 = hashInitialValue * hashMultiplier * hashMultiplier + a.hashCode() * hashMultiplier + b.hashCode()
					 = CONSTANT + a.hashCode() * hashMultiplier + b.hashCode()

				Trade-off: Balance reduced number of calculations vs increased bytecode size
			 */

			int[] multipliers = new int[getterCount + 1];
			multipliers[getterCount] = 1;
			for (int i = getterCount - 1; i >= 0; --i) {
				multipliers[i] = hashMultiplier * multipliers[i + 1];
			}
			return multipliers;
		}

	}

	private final class AreEqual {

		final String descriptorNoBridge = "(Ljava/lang/Object;Ljava/lang/Object;)Z";

		final ClassDescription cd;

		final String descriptorTypeSafe;

		AreEqual(ClassDescription cd) {
			this.cd = Objects.requireNonNull(cd);
			this.descriptorTypeSafe = "(" + consts.classToCompareDescriptor + "Ljava/lang/Object;)Z";
		}

		private void addTo(ClassWriter cw) {
			final String descriptor = implSpec.generateBridgeMethods() ? descriptorTypeSafe : descriptorNoBridge;
			final String signature = descriptorTypeSafe;
			final String getterInternalName = getterType().getInternalName();
			final String getterDescriptor = getterType().getDescriptor();
			final String areEqualDescriptor = descriptorNoBridge;

			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "areEqual", descriptor, signature, null);
			mv.visitCode();

			Label label0 = new Label();
			mv.visitLabel(label0);

			if (strictTypes()) {
				mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", "Ljava/lang/Class;");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, 3);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
			} else {
				mv.visitVarInsn(ALOAD, 1);
			}

			mv.visitVarInsn(ALOAD, 2);
			Label label1 = new Label();
			mv.visitJumpInsn(IF_ACMPNE, label1);

			Label label2 = new Label();
			mv.visitLabel(label2);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IRETURN);

			mv.visitLabel(label1);
			mv.visitFrame(F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 1);
			Label label3 = new Label();
			mv.visitJumpInsn(IFNULL, label3);
			if (strictTypes()) {
				mv.visitVarInsn(ALOAD, 3);
			} else {
				mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", "Ljava/lang/Class;");
			}
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isInstance", "(Ljava/lang/Object;)Z", false);
			Label label4 = new Label();
			mv.visitJumpInsn(IFNE, label4);
			mv.visitLabel(label3);
			mv.visitFrame(F_SAME, 0, null, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);
			mv.visitLabel(label4);
			mv.visitFrame(F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitVarInsn(ASTORE, 3);

			Label label5 = new Label();
			mv.visitLabel(label5);
			mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "getter0", getterDescriptor);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEINTERFACE, getterInternalName, "areEqual", areEqualDescriptor, true);

			Label label6 = new Label();

			for (int i = 1; i < getterCount(); ++i) {
				mv.visitJumpInsn(IFEQ, label6);
				mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "getter" + i, getterDescriptor);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitVarInsn(ALOAD, 3);
				mv.visitMethodInsn(INVOKEINTERFACE, getterInternalName, "areEqual", areEqualDescriptor, true);
			}

			mv.visitJumpInsn(IFEQ, label6);

			Label label9 = new Label();
			mv.visitLabel(label9);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IRETURN);
			mv.visitLabel(label6);
			mv.visitFrame(F_APPEND, 1, new Object[] {"java/lang/Object"}, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);

			Label label10 = new Label();
			mv.visitLabel(label10);
			mv.visitLocalVariable("self", "Ljava/lang/Object;", "TT;", label0, label10, 1);
			mv.visitLocalVariable("other", "Ljava/lang/Object;", null, label0, label10, 2);
			mv.visitLocalVariable("o", "Ljava/lang/Object;", "TT;", label5, label10, 3);
			mv.visitMaxs(0, 0);
			mv.visitEnd();

			if (implSpec.generateBridgeMethods()) {
				addBridgeMethod(cw);
			}
		}

		private void addBridgeMethod(ClassWriter cw) {
			MethodVisitor mv = cw.visitMethod(Consts.ACCESS_BRIDGE, "areEqual", "(Ljava/lang/Object;Ljava/lang/Object;)Z", null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(CHECKCAST, consts.classToCompareInternalName);
			mv.visitVarInsn(ALOAD, 2);
			String targetDescriptor = "(" + consts.classToCompareDescriptor + "Ljava/lang/Object;)Z";
			mv.visitMethodInsn(INVOKEVIRTUAL, cd.generatedInternalName, "areEqual", targetDescriptor, false);

			endReturn(mv, IRETURN);
		}

	}

	protected final class ClassDescription {

		private final Class<?> classToCompare = classToCompare();

		final String generatedInternalName = createInternalName();

		final String generatedSignature = createSignature();

		private String createInternalName() {
			String lookupClassName = Type.getInternalName(implSpec.getLookup().lookupClass());
			String classToCompare = classToCompareName();
			String prefix = Objects.requireNonNull(classNamePrefix());
			int suffix = classNameSuffix();

			return lookupClassName + "$$" + prefix + "$$" + classToCompare + "#" + suffix;
		}

		private String createSignature() {
			String signature = "Ljava/lang/Object;L";
			signature += consts.comparatorClassInternalName;
			signature += "<L";
			signature += consts.classToCompareInternalName;
			signature += ";>;";

			return signature;
		}

		private String classToCompareName() {
			return className(classToCompare);
		}

		private String className(Class<?> clazz) {
			return clazz.isArray() ? arrayName(clazz) : clazz.getName().replace('.', '$');
		}

		private String arrayName(Class<?> arrayClass) {
			Class<?> componentType = arrayClass.getComponentType();

			if (boolean.class == componentType) {
				return "booleanArray";
			} else if (byte.class == componentType) {
				return "byteArray";
			} else if (char.class == componentType) {
				return "charArray";
			} else if (double.class == componentType) {
				return "doubleArray";
			} else if (float.class == componentType) {
				return "floatArray";
			} else if (int.class == componentType) {
				return "intArray";
			} else if (long.class == componentType) {
				return "longArray";
			} else if (short.class == componentType) {
				return "shortArray";
			} else {
				return className(componentType) + "Array";
			}
		}

	}

	private static final class ExternalFieldInitializer implements FieldInitializer {

		private final Class<?> generatedClass;

		ExternalFieldInitializer(Class<?> generatedClass) {
			this.generatedClass = Objects.requireNonNull(generatedClass);
		}

		@Override
		public void addClassToCompare(Object classToCompare) {
			Internals.setStaticFieldVolatile(generatedClass, "classToCompare", classToCompare);
		}

		@Override
		public void addGetters(Collection<?> getters) {
			int i = 0;
			for (Object getter : getters) {
				Internals.setStaticFieldVolatile(generatedClass, "getter" + i++, getter);
			}
		}

		@Override
		public void addSpec(Object spec) {
			Internals.setStaticFieldVolatile(generatedClass, "spec", spec);
		}

	}

	private static final class FromSpecFieldInitializer implements FieldInitializer {

		private final MethodVisitor mv;

		private final String owner;

		private final Type specType;

		private final String specName;

		private final Consts consts;

		FromSpecFieldInitializer(MethodVisitor mv, String owner, Type specType, Consts consts) {
			this.mv = Objects.requireNonNull(mv);
			this.owner = Objects.requireNonNull(owner);
			this.specType = specType;
			this.specName = specType.getInternalName();
			this.consts = Objects.requireNonNull(consts);
		}

		@Override
		public void addClassToCompare(Object classToCompare) {
			mv.visitMethodInsn(INVOKEINTERFACE, specName, "getClassToCompare", "()Ljava/lang/Class;", true);
			mv.visitFieldInsn(PUTSTATIC, owner, "classToCompare", Consts.CLASS_DESCRIPTOR);
		}

		@Override
		public void addGetters(Collection<?> getters) {
			mv.visitMethodInsn(INVOKEINTERFACE, specName, "getGettersAsList", "()Ljava/util/List;", true);
			String getterDescriptor = consts.getterDescriptor;
			int getterCount = getters.size();

			for (int i = 0; i < getterCount; ++i) {
				if (i < getterCount - 1) {
					mv.visitInsn(DUP);
				}
				insertNumber(mv, i);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
				mv.visitFieldInsn(PUTSTATIC, owner, "getter" + i, getterDescriptor);
			}
		}

		@Override
		public void addSpec(Object spec) {
			mv.visitFieldInsn(PUTSTATIC, owner, "spec", specType.getDescriptor());
		}

	}

	protected interface FieldInitializer {

		void addClassToCompare(Object classToCompare);

		void addGetters(Collection<?> getters);

		void addSpec(Object spec);

	}

	protected static final class Consts {

		public static final Type CLASS_TYPE = Type.getType(Class.class);

		public static final String CLASS_DESCRIPTOR = CLASS_TYPE.getDescriptor();

		public static final int ACCESS_CLASS = ACC_FINAL + ACC_SUPER + ACC_SYNTHETIC;

		public static final int ACCESS_FIELD = ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_TRANSIENT;

		public static final int ACCESS_BRIDGE = ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC;

		public static final String STATIC_INITIALIZER_BRIDGE_INTERNAL_NAME = Type.getInternalName(StaticInitializerBridge.class);

		public static final String[] READ_OBJECT_EXCEPTIONS = new String[] {
				Type.getInternalName(IOException.class),
				Type.getInternalName(ClassNotFoundException.class)
		};

		public static final String READ_OBJECT_EXCEPTION_NAME = Type.getInternalName(SerializationProxyRequiredException.class);

		public static final String[] WRITE_OBJECT_EXCEPTIONS = new String[] {
				Type.getInternalName(IOException.class)
		};

		public static final String WRITE_OBJECT_EXCEPTION_NAME = Type.getInternalName(SerializationDisabledException.class);

		public final String[] interfaces;

		public final String classToCompareDescriptor;

		public final String classToCompareInternalName;

		public final String classToCompareSignature;

		public final Class<?> comparatorClass;

		public final String comparatorClassInternalName;

		public final Type getterType;

		public final String getterDescriptor;

		public final String getterInternalName;

		public final String getterSignature;

		public final Type specType;

		public final String specInternalName;

		public final String specDescriptor;

		public final String specSignature;

		Consts(AbstractAsmGenerator<?, ?, ?> generator) {
			Class<?> comparatorClass = generator.comparatorClass();
			String comparatorClassInternalName = Type.getInternalName(comparatorClass);
			Class<?> classToCompare = generator.classToCompare();

			this.interfaces = new String[] {comparatorClassInternalName};
			this.classToCompareDescriptor = Type.getDescriptor(classToCompare);
			this.classToCompareInternalName = Type.getInternalName(classToCompare);
			this.classToCompareSignature = signature("java/lang/Class");
			this.comparatorClass = comparatorClass;
			this.comparatorClassInternalName = comparatorClassInternalName;
			this.getterType = generator.getterType();
			this.getterDescriptor = getterType.getDescriptor();
			this.getterInternalName = getterType.getInternalName();
			this.getterSignature = signature(getterInternalName);
			this.specType = generator.specType();
			this.specInternalName = specType.getInternalName();
			this.specDescriptor = specType.getDescriptor();
			this.specSignature = signature(specInternalName);
		}

		private String signature(String genericTypeInternalName) {
			return "L" + genericTypeInternalName + "<" + classToCompareDescriptor + ">;";
		}

	}

}
