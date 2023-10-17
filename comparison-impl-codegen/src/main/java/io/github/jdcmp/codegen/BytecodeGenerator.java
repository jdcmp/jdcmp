package io.github.jdcmp.codegen;

import io.github.jdcmp.api.HashParameters;
import io.github.jdcmp.api.comparator.equality.EqualityComparator;
import io.github.jdcmp.api.documentation.NotThreadSafe;
import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.api.serialization.SerializationDisabledException;
import io.github.jdcmp.api.serialization.SerializationProxyRequiredException;
import io.github.jdcmp.api.spec.Spec;
import io.github.jdcmp.codegen.ClassDefiner.ClassDefinition;
import io.github.jdcmp.codegen.ClassDefiners.LookupHiddenClassWithClassDataDefiner;
import io.github.jdcmp.codegen.ClassDefiners.VMAnonymousClassDefiner;
import io.github.jdcmp.codegen.ImplSpec.OptionalClassDefiners;
import io.github.jdcmp.codegen.bridge.StaticInitializerBridge;
import io.github.jdcmp.codegen.contract.EventHandler;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode.InitializationModeMapper;
import io.github.jdcmp.codegen.customization.AvailableSerializationMode.SerializationModeMapper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F_APPEND;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.objectweb.asm.Opcodes.V16;
import static org.objectweb.asm.Opcodes.V1_8;

@ThreadSafe
abstract class BytecodeGenerator<C extends EqualityComparator<?>, U extends Spec<?, ?>> {

	protected final GeneratorConfig config;

	private final AtomicInteger instanceCounter = new AtomicInteger();

	protected BytecodeGenerator(GeneratorConfig config) {
		this.config = Objects.requireNonNull(config);
	}

	public <T extends C> T generate(U userSpec, ImplSpec implSpec) {
		validate(implSpec);
		Consts consts = new Consts(userSpec, implSpec, config);

		return generate(consts);
	}

	private <T extends C> T generate(Consts consts) {
		AvailableInitializationMode initializationMode = consts.implSpec.getInitializationMode();
		List<Callable<Genesis>> strategies = initializationMode.map(new InitializationModeMapperImpl(consts));

		if (strategies.isEmpty()) {
			throw NoStrategiesAvailableException.of(consts.implSpec);
		}

		return generate(consts, strategies);
	}

	private <T extends C> T generate(Consts consts, List<Callable<Genesis>> strategies) {
		EventHandler eventHandler = consts.implSpec.getEventHandler();

		return cast(FallbackStrategy.of(strategies).apply(callable -> {
			Genesis genesis = callable.call();
			ClassDescription cd = createClassDescription(consts);

			byte[] classBytes = genesis.generate(cd);
			eventHandler.onClassGenerated(classBytes);
			Class<C> generatedClass = genesis.define(classBytes);
			eventHandler.onClassDefined(generatedClass, classBytes);
			C instance = genesis.createInstance(generatedClass);
			eventHandler.onClassInstantiated(generatedClass, classBytes);

			return instance;
		}));
	}

	private ClassDescription createClassDescription(Consts consts) {
		int classNameSuffix = instanceCounter.getAndIncrement();

		return new ClassDescription(config, consts.userSpec, consts.implSpec, classNameSuffix);
	}

	private void validate(ImplSpec implSpec) {
		AvailableInitializationMode initializationMode = implSpec.getInitializationMode();

		if (!AvailableInitializationMode.EXTERNAL.equals(initializationMode)) {
			return;
		}

		Set<ClassDefiner> classDefiners = implSpec.getClassDefiners().all();

		if (classDefiners.stream().noneMatch(ClassDefiner::supportsExternalInitialization)) {
			String message = "None of the ClassDefiners supports external initialization: " + classDefiners;
			throw new UnsupportedOperationException(message);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends C> T cast(C comparator) {
		return (T) comparator;
	}

	abstract protected void customize(ClassWriter cw, ClassDescription cd, Consts consts);

	abstract protected void addCompatibleSerializationMethod(ClassWriter cw, ClassDescription cd);

	protected static void endReturn(MethodVisitor mv, int returnOpcode) {
		mv.visitInsn(returnOpcode);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
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

	@NotThreadSafe
	private abstract class Genesis {

		protected final Consts consts;

		protected final Instantiator instantiator;

		protected ClassDescription cd;

		protected Genesis(Consts consts, Instantiator instantiator) {
			this.consts = Objects.requireNonNull(consts);
			this.instantiator = Objects.requireNonNull(instantiator);
		}

		abstract protected int classVersion();

		abstract public Class<C> define(byte[] bytes);

		abstract public C createInstance(Class<C> generatedClass);

		public final byte[] generate(ClassDescription cd) {
			this.cd = Objects.requireNonNull(cd);

			ClassWriter cw = createClassWriter();
			addFields(cw);
			addConstructorIfNecessary(cw);
			addInterfaceImpl(cw);
			BytecodeGenerator.this.customize(cw, cd, consts);
			customize(cw);

			return end(cw);
		}

		protected void customize(ClassWriter cw) {
		}

		protected boolean isSerializable() {
			return consts.userSpec.isSerializable();
		}

		private byte[] end(ClassWriter cw) {
			cw.visitEnd();

			return cw.toByteArray();
		}

		protected ClassWriter createClassWriter() {
			String[] interfaces = consts.interfaces;
			String name = cd.generatedInternalName;
			String signature = consts.generatedClassSignature;
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			cw.visit(classVersion(), ClassConsts.ACCESS_CLASS, name, signature, "java/lang/Object", interfaces);

			return cw;
		}

		private void addFields(ClassWriter cw) {
			int access = ClassConsts.ACCESS_FIELD;

			if (isSerializable()) {
				cw.visitField(access, "spec", config.specType.descriptor, consts.specSignature, null).visitEnd();
			}

			String classToCompareSignature = consts.classToCompareSignature;
			cw.visitField(access, "classToCompare", "Ljava/lang/Class;", classToCompareSignature, null).visitEnd();

			String getterDescriptor = config.getterType.descriptor;
			String getterSignature = consts.getterSignature;
			for (int i = 0; i < consts.userSpec.getGetterCount(); ++i) {
				cw.visitField(access, "getter" + i, getterDescriptor, getterSignature, null).visitEnd();
			}
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

		final class Serialization {

			private final ClassDescription cd;

			Serialization(ClassDescription cd) {
				this.cd = Objects.requireNonNull(cd);
			}

			void addTo(ClassWriter cw) {
				consts.implSpec.getSerializationMode().map(new SerializationModeMapper<Void>() {
					@Override
					public Void onCompatible() {
						addCompatibleSerializationMethod(cw, cd);
						addHostileReadObject(cw);
						return null;
					}

					@Override
					public Void onCompatibleUnprotected() {
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
				String exceptionName = ClassConsts.READ_OBJECT_EXCEPTION_NAME;
				MethodVisitor mv = classWriter.visitMethod(ACC_PRIVATE, "readObject",
						"(Ljava/io/ObjectInputStream;)V", null, ClassConsts.READ_OBJECT_EXCEPTIONS);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitTypeInsn(NEW, exceptionName);
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, exceptionName, "<init>", "()V", false);
				endReturn(mv, ATHROW);
			}

			private void addHostileWriteObject(ClassWriter cw) {
				String exceptionName = ClassConsts.WRITE_OBJECT_EXCEPTION_NAME;
				MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "writeObject",
						"(Ljava/io/ObjectOutputStream;)V", null, ClassConsts.WRITE_OBJECT_EXCEPTIONS);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitTypeInsn(NEW, exceptionName);
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, exceptionName, "<init>", "()V", false);
				endReturn(mv, ATHROW);
			}

		}

		final class Hash {

			final ClassDescription cd;

			final String descriptorBridge;

			Hash(ClassDescription cd) {
				this.cd = cd;
				this.descriptorBridge = "(" + consts.classToCompare.descriptor + ")I";
			}

			void addTo(ClassWriter cw) {
				final String descriptor = generateDescriptor();
				final String signature = "(" + consts.classToCompare.descriptor + ")I";
				final String getterInternalName = config.getterType.internalName;
				final String getterDescriptor = config.getterType.descriptor;
				final String hashDescriptor = "(Ljava/lang/Object;)I";

				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hash", descriptor, signature, null);
				mv.visitCode();

				mv.visitVarInsn(ALOAD, 1);
				Label label0 = new Label();
				mv.visitJumpInsn(IFNONNULL, label0);
				mv.visitInsn(ICONST_0);
				mv.visitInsn(IRETURN);
				mv.visitLabel(label0);

				int getterCount = consts.userSpec.getGetterCount();
				HashParameters hashParameters = consts.userSpec.getHashParameters();
				int[] multipliers = calculateMultipliers(hashParameters.multiplier(), getterCount);
				insertNumber(mv, hashParameters.initialValue() * multipliers[0]);

				for (int i = 0; i < getterCount; ++i) {
					mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "getter" + i, getterDescriptor);

					if (consts.userSpec.useStrictTypes() && i == 0) {
						mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", ClassConsts.CLASS_DESCRIPTOR);
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

				if (consts.implSpec.generateBridgeMethods()) {
					addBridgeMethod(cw);
				}
			}

			private String generateDescriptor() {
				return consts.implSpec.generateBridgeMethods() ? "(" + consts.classToCompare.descriptor + ")I" : "(Ljava/lang/Object;)I";
			}

			private void addBridgeMethod(ClassWriter cw) {
				MethodVisitor mv = cw.visitMethod(ClassConsts.ACCESS_BRIDGE, "hash", "(Ljava/lang/Object;)I", null, null);
				mv.visitCode();

				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, consts.classToCompare.internalName);
				String targetDescriptor = "(" + consts.classToCompare.descriptor + ")I";
				mv.visitMethodInsn(INVOKEVIRTUAL, cd.generatedInternalName, "hash", targetDescriptor, false);

				endReturn(mv, IRETURN);
			}

			private int[] calculateMultipliers(int hashMultiplier, int getterCount) {
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

		final class AreEqual {

			final ClassDescription cd;

			AreEqual(ClassDescription cd) {
				this.cd = Objects.requireNonNull(cd);
			}

			void addTo(ClassWriter cw) {
				final String descriptorTypeSafe = "(" + consts.classToCompare.descriptor + "Ljava/lang/Object;)Z";
				final String descriptorNoBridge = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
				final String descriptor = consts.implSpec.generateBridgeMethods() ? descriptorTypeSafe : descriptorNoBridge;
				final String signature = descriptorTypeSafe;
				final String getterInternalName = config.getterType.internalName;
				final String getterDescriptor = config.getterType.descriptor;
				final String areEqualDescriptor = descriptorNoBridge;

				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "areEqual", descriptor, signature, null);
				mv.visitCode();

				Label label0 = new Label();
				mv.visitLabel(label0);

				putObjectsToCompareOntoStack(mv);

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
				if (consts.userSpec.useStrictTypes()) {
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

				for (int i = 1; i < consts.userSpec.getGetterCount(); ++i) {
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

				if (consts.implSpec.generateBridgeMethods()) {
					addBridgeMethod(cw);
				}
			}

			private void putObjectsToCompareOntoStack(MethodVisitor mv) {
				if (consts.userSpec.useStrictTypes()) {
					mv.visitFieldInsn(GETSTATIC, cd.generatedInternalName, "classToCompare", "Ljava/lang/Class;");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, 3);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "cast", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
				} else {
					mv.visitVarInsn(ALOAD, 1);
				}

				mv.visitVarInsn(ALOAD, 2);
			}

			private void addBridgeMethod(ClassWriter cw) {
				MethodVisitor mv = cw.visitMethod(ClassConsts.ACCESS_BRIDGE, "areEqual", "(Ljava/lang/Object;Ljava/lang/Object;)Z", null, null);
				mv.visitCode();

				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, consts.classToCompare.internalName);
				mv.visitVarInsn(ALOAD, 2);
				String targetDescriptor = "(" + consts.classToCompare.descriptor + "Ljava/lang/Object;)Z";
				mv.visitMethodInsn(INVOKEVIRTUAL, cd.generatedInternalName, "areEqual", targetDescriptor, false);

				endReturn(mv, IRETURN);
			}

		}

	}

	private abstract class GenesisStaticInitializer extends Genesis {

		protected GenesisStaticInitializer(Consts consts, Instantiator instantiator) {
			super(consts, instantiator);
		}

		abstract protected void visitPutSpecOnStack(MethodVisitor mv);

		@Override
		protected void customize(ClassWriter cw) {
			addStaticInitializer(cw);
		}

		private void addStaticInitializer(ClassWriter cw) {
			MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();

			visitPutSpecOnStack(mv);
			visitInitializeFromSpec(mv);

			endReturn(mv, RETURN);
		}

		private void visitInitializeFromSpec(MethodVisitor mv) {
			FromSpecFieldInitializer init = new FromSpecFieldInitializer(mv, cd);
			Spec<?, ?> userSpec = consts.userSpec;

			if (isSerializable()) {
				mv.visitInsn(DUP);
				init.addSpec(userSpec);
			}

			mv.visitInsn(DUP);
			init.addClassToCompare(userSpec);
			init.addGetters(userSpec);
		}

	}

	private final class VmAnonymousStrategy extends GenesisStaticInitializer {

		private final VMAnonymousClassDefiner classDefiner;

		private int constantPoolIndex;

		VmAnonymousStrategy(Consts consts, Instantiator instantiator, VMAnonymousClassDefiner classDefiner) {
			super(consts, instantiator);
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
		protected void visitPutSpecOnStack(MethodVisitor mv) {
			mv.visitLdcInsn("PLACEHOLDER_SPEC");
			mv.visitTypeInsn(CHECKCAST, config.specType.internalName);
		}

		@Override
		public Class<C> define(byte[] bytes) {
			Lookup host = consts.implSpec.getLookup();
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, host);
			Object[] constantPoolPatches = createConstantPoolPatches();

			return classDefiner.defineClass(classDefinition, constantPoolPatches);
		}

		private Object[] createConstantPoolPatches() {
			int cpi = constantPoolIndex;
			Object[] constantPoolPatches = new Object[cpi + 1];
			constantPoolPatches[cpi] = consts.userSpec;

			return constantPoolPatches;
		}

		@Override
		public C createInstance(Class<C> generatedClass) {
			return instantiator.newInstance(generatedClass);
		}

	}

	private final class LookupHiddenWithClassDataStrategy extends GenesisStaticInitializer {

		final LookupHiddenClassWithClassDataDefiner classDefiner;

		LookupHiddenWithClassDataStrategy(Consts consts, Instantiator instantiator, LookupHiddenClassWithClassDataDefiner classDefiner) {
			super(consts, instantiator);
			this.classDefiner = Objects.requireNonNull(classDefiner);
		}

		@Override
		protected int classVersion() {
			return V16;
		}

		@Override
		protected void visitPutSpecOnStack(MethodVisitor mv) {
			Handle handle = LookupHiddenHolder.CLASS_DATA_HANDLE;

			mv.visitLdcInsn(new ConstantDynamic("_", config.specType.descriptor, handle));
		}

		@Override
		public Class<C> define(byte[] bytes) {
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, consts.implSpec.getLookup());

			return classDefiner.defineClass(classDefinition, consts.userSpec);
		}

		@Override
		public C createInstance(Class<C> generatedClass) {
			return instantiator.newInstance(generatedClass);
		}

	}

	private static final class LookupHiddenHolder {

		static final Handle CLASS_DATA_HANDLE;

		static {
			try {
				Method classData = MethodHandles.class.getDeclaredMethod("classData", Lookup.class, String.class, Class.class);
				String owner = Type.getInternalName(MethodHandles.class);
				String descriptor = Type.getMethodDescriptor(classData);
				CLASS_DATA_HANDLE = new Handle(H_INVOKESTATIC, owner, "classData", descriptor, false);
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e);
			}
		}

	}

	private final class StaticInitializerBridgeStrategy extends GenesisStaticInitializer {

		private final ClassDefiner classDefiner;

		StaticInitializerBridgeStrategy(Consts consts, Instantiator instantiator, ClassDefiner classDefiner) {
			super(consts, instantiator);
			this.classDefiner = Objects.requireNonNull(classDefiner);
		}

		@Override
		protected int classVersion() {
			return V1_8;
		}

		@Override
		protected void visitPutSpecOnStack(MethodVisitor mv) {
			mv.visitMethodInsn(
					INVOKESTATIC,
					"java/lang/invoke/MethodHandles",
					"lookup",
					"()Ljava/lang/invoke/MethodHandles$Lookup;",
					false);
			mv.visitMethodInsn(
					INVOKESTATIC,
					ClassConsts.STATIC_INITIALIZER_BRIDGE_INTERNAL_NAME,
					config.staticBridgeMethod.getName(),
					config.staticBridgeMethodDescriptor,
					false);
		}

		@Override
		public Class<C> define(byte[] bytes) {
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, consts.implSpec.getLookup());

			return classDefiner.defineClass(classDefinition);
		}

		@Override
		public C createInstance(Class<C> generatedClass) {
			return StaticInitializerBridge.run(generatedClass, consts.userSpec, () -> instantiator.newInstance(generatedClass));
		}

	}

	private final class ExternalInitializationStrategy extends Genesis {

		private final ClassDefiner classDefiner;

		ExternalInitializationStrategy(Consts consts, Instantiator instantiator, ClassDefiner classDefiner) {
			super(consts, instantiator);
			this.classDefiner = Objects.requireNonNull(classDefiner);
		}

		@Override
		protected int classVersion() {
			return V1_8;
		}

		@Override
		public Class<C> define(byte[] bytes) {
			ClassDefinition classDefinition = new ClassDefinition(bytes, cd.generatedInternalName, consts.implSpec.getLookup());

			return classDefiner.defineClass(classDefinition);
		}

		@Override
		public C createInstance(Class<C> generatedClass) {
			C instance = instantiator.newInstance(generatedClass);
			initializeFieldsExternally(instance.getClass());

			return instance;
		}

		private void initializeFieldsExternally(Class<?> generatedClass) {
			ExternalFieldInitializer fieldInitializer = new ExternalFieldInitializer(generatedClass);
			Spec<?, ?> userSpec = consts.userSpec;

			if (isSerializable()) {
				fieldInitializer.addSpec(userSpec);
			}

			fieldInitializer.addClassToCompare(userSpec);
			fieldInitializer.addGetters(userSpec);
		}

	}

	private static final class ExternalFieldInitializer {

		private final Class<?> generatedClass;

		ExternalFieldInitializer(Class<?> generatedClass) {
			this.generatedClass = Objects.requireNonNull(generatedClass);
		}

		public void addClassToCompare(Spec<?, ?> spec) {
			Internals.setStaticFieldVolatile(generatedClass, "classToCompare", spec.getClassToCompare());
		}

		public void addGetters(Spec<?, ?> spec) {
			int i = 0;
			for (Object getter : spec.getGetters()) {
				Internals.setStaticFieldVolatile(generatedClass, "getter" + i++, getter);
			}
		}

		public void addSpec(Spec<?, ?> spec) {
			Internals.setStaticFieldVolatile(generatedClass, "spec", spec);
		}

	}

	private final class FromSpecFieldInitializer {

		private final MethodVisitor mv;

		private final String owner;

		FromSpecFieldInitializer(MethodVisitor mv, ClassDescription cd) {
			this.mv = Objects.requireNonNull(mv);
			this.owner = cd.generatedInternalName;
		}

		public void addClassToCompare(Spec<?, ?> spec) {
			mv.visitMethodInsn(INVOKEINTERFACE, config.specType.internalName, "getClassToCompare", "()Ljava/lang/Class;", true);
			mv.visitFieldInsn(PUTSTATIC, owner, "classToCompare", ClassConsts.CLASS_DESCRIPTOR);
		}

		public void addGetters(Spec<?, ?> spec) {
			mv.visitMethodInsn(INVOKEINTERFACE, config.specType.internalName, "getGettersAsList", "()Ljava/util/List;", true);
			String getterDescriptor = config.getterType.descriptor;
			int getterCount = spec.getGetterCount();

			for (int i = 0; i < getterCount; ++i) {
				if (i < getterCount - 1) {
					mv.visitInsn(DUP);
				}
				insertNumber(mv, i);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
				mv.visitFieldInsn(PUTSTATIC, owner, "getter" + i, getterDescriptor);
			}
		}

		public void addSpec(Spec<?, ?> spec) {
			mv.visitFieldInsn(PUTSTATIC, owner, "spec", config.specType.descriptor);
		}

	}

	static final class ClassDescription {

		public final String generatedInternalName;

		ClassDescription(GeneratorConfig config, Spec<?, ?> userSpec, ImplSpec implSpec, int classNameSuffix) {
			this.generatedInternalName = internalName(config, userSpec, implSpec, classNameSuffix);
		}

		private static String internalName(GeneratorConfig config, Spec<?, ?> userSpec, ImplSpec implSpec, int classNameSuffix) {
			String lookupClassName = Type.getInternalName(implSpec.getLookup().lookupClass());
			String prefix = config.generatedClassNamePrefix;
			String classToCompare = className(userSpec.getClassToCompare());

			return lookupClassName + "$$" + prefix + "$$" + classToCompare + "#" + classNameSuffix;
		}

		private static String className(Class<?> clazz) {
			return clazz.isArray() ? arrayName(clazz) : clazz.getName().replace('.', '$');
		}

		private static String arrayName(Class<?> arrayClass) {
			Class<?> componentType = arrayClass.getComponentType();
			String componentName = componentType.isPrimitive() ? componentType.getName() : className(componentType);

			return componentName + "Array";
		}

	}

	static final class GeneratorConfig {

		public final ConstType comparatorType;

		public final ConstType specType;

		public final ConstType getterType;

		public final String generatedClassNamePrefix;

		public final Method staticBridgeMethod;

		public final String staticBridgeMethodDescriptor;

		public GeneratorConfig(
				Class<?> comparatorType,
				Class<?> specType,
				Class<?> getterType,
				String generatedClassNamePrefix,
				Method staticBridgeMethod) {
			this.comparatorType = new ConstType(comparatorType);
			this.specType = new ConstType(specType);
			this.getterType = new ConstType(getterType);
			this.generatedClassNamePrefix = Objects.requireNonNull(generatedClassNamePrefix);
			this.staticBridgeMethod = staticBridgeMethod;
			this.staticBridgeMethodDescriptor = Type.getMethodDescriptor(staticBridgeMethod);
		}

	}

	static class ConstType {

		public final Class<?> clazz;

		public final String internalName;

		public final String descriptor;

		public ConstType(Class<?> clazz) {
			this.clazz = Objects.requireNonNull(clazz);
			this.internalName = Type.getInternalName(clazz);
			this.descriptor = Type.getDescriptor(clazz);
		}

	}

	static final class ClassConsts {

		static final Type CLASS_TYPE = Type.getType(Class.class);

		static final String CLASS_DESCRIPTOR = CLASS_TYPE.getDescriptor();

		static final int ACCESS_CLASS = ACC_FINAL + ACC_SUPER + ACC_SYNTHETIC;

		static final int ACCESS_FIELD = ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_TRANSIENT;

		static final int ACCESS_BRIDGE = ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC;

		static final String STATIC_INITIALIZER_BRIDGE_INTERNAL_NAME = Type.getInternalName(StaticInitializerBridge.class);

		static final String[] READ_OBJECT_EXCEPTIONS = new String[] {
				Type.getInternalName(IOException.class),
				Type.getInternalName(ClassNotFoundException.class)
		};

		static final String READ_OBJECT_EXCEPTION_NAME = Type.getInternalName(SerializationProxyRequiredException.class);

		static final String[] WRITE_OBJECT_EXCEPTIONS = new String[] {
				Type.getInternalName(IOException.class)
		};

		static final String WRITE_OBJECT_EXCEPTION_NAME = Type.getInternalName(SerializationDisabledException.class);

	}

	final class Consts {

		public final U userSpec;

		public final ImplSpec implSpec;

		public final String[] interfaces;

		public final ConstType classToCompare;

		public final String classToCompareSignature;

		public final String getterSignature;

		public final String specSignature;

		public final String generatedClassSignature;

		Consts(U userSpec, ImplSpec implSpec, GeneratorConfig config) {
			this.userSpec = Objects.requireNonNull(userSpec);
			this.implSpec = Objects.requireNonNull(implSpec);
			this.interfaces = new String[] {config.comparatorType.internalName};
			this.classToCompare = new ConstType(userSpec.getClassToCompare());
			this.classToCompareSignature = "Ljava/lang/Class<" + classToCompare.descriptor + ">;";
			this.getterSignature = "L" + config.getterType.internalName + "<-" + classToCompare.descriptor + ">;";
			this.specSignature = "L" + config.specType.internalName + "<" + classToCompare.descriptor + ">;";
			this.generatedClassSignature = generateSignatureForGeneratedClass(config);
		}

		private String generateSignatureForGeneratedClass(GeneratorConfig config) {
			String signature = "Ljava/lang/Object;L";
			signature += config.comparatorType.internalName;
			signature += "<L";
			signature += classToCompare.internalName;

			return signature + ";>;";
		}

	}

	private final class InitializationModeMapperImpl implements InitializationModeMapper<List<Callable<Genesis>>> {

		private final Consts consts;

		private InitializationModeMapperImpl(Consts consts) {
			this.consts = Objects.requireNonNull(consts);
		}

		@Override
		public List<Callable<Genesis>> onStaticInitializer() {
			ImplSpec implSpec = consts.implSpec;
			OptionalClassDefiners classDefiners = implSpec.getClassDefiners();
			List<Instantiator> instantiators = implSpec.getInstantiators();
			List<Callable<Genesis>> list = new ArrayList<>();

			classDefiners.getVmAnonymousClassDefiner().ifPresent(classDefiner -> {
				for (Instantiator instantiator : instantiators) {
					if (instantiator.supports(classDefiner)) {
						list.add(() -> new VmAnonymousStrategy(consts, instantiator, classDefiner));
					}
				}
			});

			classDefiners.getLookupHiddenClassWithClassDataDefiner().ifPresent(classDefiner -> {
				for (Instantiator instantiator : instantiators) {
					if (instantiator.supports(classDefiner)) {
						list.add(() -> new LookupHiddenWithClassDataStrategy(consts, instantiator, classDefiner));
					}
				}
			});

			for (ClassDefiner classDefiner : classDefiners.all()) {
				for (Instantiator instantiator : instantiators) {
					if (instantiator.supports(classDefiner)) {
						list.add(() -> new StaticInitializerBridgeStrategy(consts, instantiator, classDefiner));
					}
				}
			}

			return list;
		}

		@Override
		public List<Callable<Genesis>> onExternal() {
			ImplSpec implSpec = consts.implSpec;
			OptionalClassDefiners classDefiners = implSpec.getClassDefiners();
			List<Instantiator> instantiators = implSpec.getInstantiators();
			List<Callable<Genesis>> list = new ArrayList<>();

			for (ClassDefiner classDefiner : classDefiners.all()) {
				if (classDefiner.supportsExternalInitialization()) {
					for (Instantiator instantiator : instantiators) {
						list.add(() -> new ExternalInitializationStrategy(consts, instantiator, classDefiner));
					}
				}
			}

			return list;
		}

	}

}
