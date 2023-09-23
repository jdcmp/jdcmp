package io.github.jdcmp.codegen;


import io.github.jdcmp.api.documentation.ThreadSafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@ThreadSafe
final class Internals {

	private static final Logger LOGGER = Logger.getLogger(Internals.class.getName());

	private static final FallbackStrategy<FieldFinder> FIELD_FINDER_STRATEGIES;

	private static final FallbackStrategy<FieldSetter> FIELD_ACCESS_STRATEGIES;

	static {
		ArrayList<FieldFinder> strategies = new ArrayList<>(2);
		add(strategies, TrustedLookupFieldFinder.class, TrustedLookupFieldFinder::new);
		add(strategies, PlainFieldFinder.class, PlainFieldFinder::new);
		FIELD_FINDER_STRATEGIES = FallbackStrategy.of(strategies);
	}

	static {
		ArrayList<FieldSetter> strategies = new ArrayList<>(2);
		add(strategies, UnsafeFieldSetter.class, UnsafeFieldSetter::new);
		add(strategies, PlainFieldSetter.class, PlainFieldSetter::new);
		FIELD_ACCESS_STRATEGIES = FallbackStrategy.of(strategies);
	}

	private static <T> void add(ArrayList<T> list, Class<? extends T> clazz, Supplier<T> factory) {
		try {
			list.add(factory.get());
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Class for Field handling is unavailable: " + clazz, e);
		}
	}

	public static void setStaticFieldVolatile(Class<?> clazz, String fieldName, Object value) {
		Field field = FIELD_FINDER_STRATEGIES.apply(ff -> ff.findStatic(clazz, fieldName));
		FIELD_ACCESS_STRATEGIES.consume(fa -> fa.setStatic(field, value));
	}

	public static MethodHandle superUnreflect(Method method) {
		return TrustedLookup.unreflect(method);
	}

	static final class Unsafe {

		private static final Class<?> CLASS = OptionalClass.UNSAFE.load();

		private static final Object INSTANCE = SingletonFinder.findInstance(CLASS);

		static Object getInstance() {
			return INSTANCE;
		}

		static final class Method extends AbstractMethod {

			static final Method STATIC_FIELD_BASE = of("staticFieldBase", Object.class, Field.class);

			static final Method STATIC_FIELD_OFFSET = of("staticFieldOffset", long.class, Field.class);

			static final Method PUT_OBJECT_VOLATILE = of("putObjectVolatile", void.class, Object.class, long.class, Object.class);

			static final Method ALLOCATE_INSTANCE = of("allocateInstance", Object.class, Class.class);

			static final Method DEFINE_ANONYMOUS_CLASS = of("defineAnonymousClass", Class.class, Class.class, byte[].class, Object[].class);

			private static Method of(String methodName, Class<?> returnType, Class<?>... parameterTypes) {
				return new Method(methodName, MethodType.methodType(returnType, parameterTypes));
			}

			private Method(String methodName, MethodType methodType) {
				super(OptionalClass.UNSAFE, methodName, methodType);
			}

		}

	}

	static final class ReflectionFactory {

		private static final Object INSTANCE = SingletonFinder.findInstance(OptionalClass.REFLECTION_FACTORY.load());

		public static Object getInstance() {
			return INSTANCE;
		}

		static final class Method extends AbstractMethod {

			static final Method NEW_CONSTRUCTOR_FOR_SERIALIZATION = of("newConstructorForSerialization", Constructor.class, Class.class);

			static final Method NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR = of("newConstructorForSerialization", Constructor.class, Class.class, Constructor.class);

			private static Method of(String methodName, Class<?> returnType, Class<?>... parameterTypes) {
				return new Method(methodName, MethodType.methodType(returnType, parameterTypes));
			}

			private Method(String methodName, MethodType methodType) {
				super(OptionalClass.REFLECTION_FACTORY, methodName, methodType);
			}

		}

	}

	static final class Lookup {

		static final class Method extends AbstractMethod {

			static final Method DEFINE_CLASS = of("defineClass", Class.class, byte[].class);

			static final Method ACCESS_CLASS = of("accessClass", Class.class, Class.class);

			private static Method of(String methodName, Class<?> returnType, Class<?>... parameterTypes) {
				return new Method(methodName, MethodType.methodType(returnType, parameterTypes));
			}

			Method(String methodName, MethodType methodType) {
				super(OptionalClass.LOOKUP, methodName, methodType);
			}

		}

	}

	abstract static class AbstractMethod implements EnumeratedMethod {

		private final OptionalClass methodDeclaringClass;

		private final String methodName;

		private final MethodType methodType;

		protected AbstractMethod(OptionalClass methodDeclaringClass, String methodName, MethodType methodType) {
			this.methodDeclaringClass = Objects.requireNonNull(methodDeclaringClass);
			this.methodName = Objects.requireNonNull(methodName);
			this.methodType = Objects.requireNonNull(methodType);
		}

		@Override
		public OptionalClass getMethodDeclaringClass() {
			return methodDeclaringClass;
		}

		@Override
		public String getMethodName() {
			return methodName;
		}

		@Override
		public MethodType getMethodType() {
			return methodType;
		}

	}

	enum OptionalClass {

		UNSAFE("sun.misc.Unsafe"),
		REFLECTION_FACTORY("sun.reflect.ReflectionFactory"),
		LOOKUP("java.lang.invoke.MethodHandles$Lookup"),
		CLASS_OPTION("java.lang.invoke.MethodHandles$Lookup$ClassOption");

		private final String className;

		OptionalClass(String className) {
			this.className = Objects.requireNonNull(className);
		}

		public String getClassName() {
			return className;
		}

		public Class<?> load() {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private static final class MethodHandleFinder {

		private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

		public static MethodHandle find(MethodHandleMetadata info) {
			OptionalClass declaringClass = info.getMethodDeclaringClass();
			String methodName = info.getMethodName();
			MethodType methodType = info.getMethodType();

			try {
				Class<?> clazz = declaringClass.load();

				return LOOKUP.findVirtual(clazz, methodName, methodType);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Failed to find method: " + declaringClass.getClassName() + "." + methodName + methodType);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	private interface EnumeratedMethod extends MethodHandleMetadata, MethodHandleResolver {

		@Override
		default MethodHandle find() {
			return MethodHandleFinder.find(this);
		}

		default Optional<MethodHandle> tryFind() {
			try {
				return Optional.of(find());
			} catch (Exception e) {
				return Optional.empty();
			}
		}

	}

	private interface MethodHandleMetadata {

		OptionalClass getMethodDeclaringClass();

		String getMethodName();

		MethodType getMethodType();

	}

	private interface MethodHandleResolver {

		MethodHandle find();

		Optional<MethodHandle> tryFind();

	}

	private static final class SingletonFinder {

		static Object findInstance(Class<?> clazz) {
			try {
				for (Field field : clazz.getDeclaredFields()) {
					if (field.getType() == clazz && Modifier.isStatic(field.getModifiers())) {
						field.setAccessible(true);
						Object object = field.get(null);

						if (object != null) {
							return object;
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to find field containing instance of class: " + clazz, e);
			}

			throw new RuntimeException("Failed to find field containing instance of class: " + clazz);
		}

	}

	/**
	 * No longer works in Java 17+ (missing sun.misc.Unsafe.defineAnonymousClass)
	 */
	static final class TrustedLookup {

		// A minimalistic class with "static Lookup get() { return Lookup.IMPL_LOOKUP; }", encoded in base64
		private static final String ACCESSOR = "yv66vgAAADQADgEANWphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcyQkVHJ1c3RlZExvb2t" +
				"1cEFjY2Vzc29yBwABAQAQamF2YS9sYW5nL09iamVjdAcAAwEAA2dldAEAKSgpTGphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGx" +
				"lcyRMb29rdXA7AQAlamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGVzJExvb2t1cAcABwEAC0lNUExfTE9PS1VQAQAnTGphdmE" +
				"vbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcyRMb29rdXA7DAAJAAoJAAgACwEABENvZGUAIQACAAQAAAAAAAEACQAFAAYAAQANAAA" +
				"AEAABAAAAAAAEsgAMsAAAAAAAAA==";

		public static MethodHandle unreflect(Method method) {
			try {
				return get().unreflect(method);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		public static MethodHandles.Lookup get() {
			try {
				byte[] bytes = Base64.getDecoder().decode(ACCESSOR);
				Class<?> accessorClass = (Class<?>) Holder.DEFINE_ANONYMOUS_CLASS
						.invokeExact(MethodHandles.class, bytes, (Object[]) null);
				Method accessorMethod = accessorClass.getDeclaredMethod("get");

				return (MethodHandles.Lookup) accessorMethod.invoke(null);
			} catch (@SuppressWarnings("removal") RuntimeException | ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		private static final class Holder {

			static final MethodHandle DEFINE_ANONYMOUS_CLASS
					= Unsafe.Method.DEFINE_ANONYMOUS_CLASS.find().bindTo(Unsafe.getInstance());

		}

	}

	@ThreadSafe
	interface FieldFinder {

		Field findStatic(Class<?> clazz, String fieldName) throws Throwable;

	}

	@ThreadSafe
	static final class PlainFieldFinder implements FieldFinder {

		@Override
		public Field findStatic(Class<?> clazz, String fieldName) throws Throwable {
			return clazz.getDeclaredField(fieldName);
		}

	}

	@ThreadSafe
	static final class TrustedLookupFieldFinder implements FieldFinder {

		TrustedLookupFieldFinder() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public Field findStatic(Class<?> clazz, String fieldName) throws Throwable {
			Field[] fields = (Field[]) Holder.GET_DECLARED_FIELDS_0.invokeExact(clazz, false);

			for (Field field : fields) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}

			throw new NoSuchFieldException("No field " + fieldName + " in class " + clazz);
		}

		private static final class Holder {

			static final MethodHandle GET_DECLARED_FIELDS_0;

			static {
				try {
					MethodHandles.Lookup lookup = TrustedLookup.get();
					MethodType methodType = MethodType.methodType(Field[].class, boolean.class);
					GET_DECLARED_FIELDS_0 = lookup.findSpecial(Class.class, "getDeclaredFields0", methodType, Class.class);
				} catch (Exception e) {
					throw new ExceptionInInitializerError(e);
				}
			}

		}

	}

	@ThreadSafe
	interface FieldSetter {

		void setStatic(Field field, Object value) throws Throwable;

	}

	static final class UnsafeFieldSetter implements FieldSetter {

		UnsafeFieldSetter() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public void setStatic(Field field, Object value) throws Throwable {
			Object base = Holder.STATIC_FIELD_BASE.invokeExact(field);
			long offset = (long) Holder.STATIC_FIELD_OFFSET.invokeExact(field);

			Holder.PUT_OBJECT_VOLATILE.invokeExact(base, offset, value);
		}

		private static final class Holder {

			private static final MethodHandle STATIC_FIELD_BASE;

			private static final MethodHandle STATIC_FIELD_OFFSET;

			private static final MethodHandle PUT_OBJECT_VOLATILE;

			static {
				Class<?> unsafeClass = OptionalClass.UNSAFE.load();
				Object unsafeInstance = SingletonFinder.findInstance(unsafeClass);
				STATIC_FIELD_BASE = Unsafe.Method.STATIC_FIELD_BASE.find().bindTo(unsafeInstance);
				STATIC_FIELD_OFFSET = Unsafe.Method.STATIC_FIELD_OFFSET.find().bindTo(unsafeInstance);
				PUT_OBJECT_VOLATILE = Unsafe.Method.PUT_OBJECT_VOLATILE.find().bindTo(unsafeInstance);
			}

		}

	}

	static final class PlainFieldSetter implements FieldSetter {

		@Override
		public void setStatic(Field field, Object value) throws Throwable {
			int modifiers = field.getModifiers();

			if (Modifier.isFinal(modifiers)) {
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.set(field, modifiers & ~Modifier.FINAL);
			}

			field.setAccessible(true);
			field.set(null, value);
		}

	}

	private Internals() {
		throw new AssertionError("No instances");
	}

}
