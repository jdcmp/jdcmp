package io.github.jdcmp.codegen;

import io.github.jdcmp.api.documentation.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

@ThreadSafe
final class ClassDefiners {

	@ThreadSafe
	static final class ClassLoaderClassDefiner implements ClassDefiner {

		ClassLoaderClassDefiner() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean supportsExternalInitialization() {
			return true;
		}

		@Override
		public boolean producesVmAnonymousOrHiddenClasses() {
			return false;
		}

		public <T> Class<T> defineClass(ClassDefinition classDefinition) {
			String name = classDefinition.getName();
			byte[] bytes = classDefinition.getBytes();
			Class<?> hostClass = classDefinition.getHost().lookupClass();
			ClassLoader hostClassLoader = Utils.verifyHasClassLoader(hostClass);

			try {
				@SuppressWarnings("unchecked")
				Class<T> clazz = (Class<T>) Holder.DEFINE_CLASS.invokeExact(
						hostClassLoader,
						(String) null,
						bytes,
						0,
						bytes.length);

				return clazz;
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw ClassDefinerException.forClass(name, e);
			}
		}

		private static final class Holder {

			static final MethodHandle DEFINE_CLASS;

			static {
				try {
					Method defineClass = ClassLoader.class.getDeclaredMethod(
							"defineClass",
							String.class,
							byte[].class,
							int.class,
							int.class);
					DEFINE_CLASS = Internals.superUnreflect(defineClass);
				} catch (NoSuchMethodException e) {
					throw new ExceptionInInitializerError(e);
				}
			}

		}

	}

	@ThreadSafe
	static final class LookupClassDefiner implements ClassDefiner {

		LookupClassDefiner() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean supportsExternalInitialization() {
			return true;
		}

		@Override
		public boolean producesVmAnonymousOrHiddenClasses() {
			return false;
		}

		public <T> Class<T> defineClass(ClassDefinition classDefinition) {
			Lookup lookup = classDefinition.getHost();
			Utils.verifyHasClassLoader(lookup.lookupClass());

			try {
				@SuppressWarnings("unchecked")
				Class<T> clazz = (Class<T>) Holder.DEFINE_CLASS.invokeExact(lookup, classDefinition.getBytes());

				return clazz;
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw ClassDefinerException.forClass(classDefinition.getName(), e);
			}
		}

		private static final class Holder {

			static final MethodHandle DEFINE_CLASS = Internals.Lookup.Method.DEFINE_CLASS.find();

		}

	}

	/**
	 * Leverages Java 15+ APIs to create more lightweight classes. The JVM seems to optimize hidden and
	 * VM-anonymous classes (among other things) more aggressively than regular classes.
	 * See {@link VMAnonymousClassDefiner} for more details.
	 */
	@ThreadSafe
	static final class LookupHiddenClassDefiner implements ClassDefiner {

		LookupHiddenClassDefiner() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean supportsExternalInitialization() {
			return false;
		}

		@Override
		public boolean producesVmAnonymousOrHiddenClasses() {
			return true;
		}

		public <T> Class<T> defineClass(ClassDefinition classDefinition) {
			Lookup host = classDefinition.getHost();
			Utils.verifyHasClassLoader(host.lookupClass());
			byte[] bytes = classDefinition.getBytes();

			try {
				Lookup definedLookup = (Lookup) Holder.DEFINE_HIDDEN_CLASS.invokeExact(
						host,
						bytes,
						false,
						Holder.EMPTY_CLASS_OPTIONS);

				return cast(definedLookup);
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw ClassDefinerException.forClass(classDefinition.getName(), e);
			}
		}

		@SuppressWarnings("unchecked")
		private static <T> Class<T> cast(Lookup definedLookup) {
			return (Class<T>) definedLookup.lookupClass();
		}

		private static final class Holder {

			static final MethodHandle DEFINE_HIDDEN_CLASS;

			static final Object EMPTY_CLASS_OPTIONS; // ClassOption[0]

			static {
				try {
					EMPTY_CLASS_OPTIONS = Array.newInstance(Internals.OptionalClass.CLASS_OPTION.load(), 0);
					Class<?> classOptionArrayClass = EMPTY_CLASS_OPTIONS.getClass();
					MethodType methodType = MethodType.methodType(Lookup.class, byte[].class, boolean.class, classOptionArrayClass);
					Lookup lookup = MethodHandles.lookup();
					MethodHandle defineHiddenClass = lookup.findVirtual(Lookup.class, "defineHiddenClass", methodType).asFixedArity();
					DEFINE_HIDDEN_CLASS = defineHiddenClass.asType(defineHiddenClass.type().changeParameterType(3, Object.class));
				} catch (@SuppressWarnings("removal") ThreadDeath e) {
					throw e;
				} catch (Throwable e) {
					throw new ExceptionInInitializerError(e);
				}
			}

		}

	}

	@ThreadSafe
	static final class LookupHiddenClassWithClassDataDefiner implements ClassDefiner {

		LookupHiddenClassWithClassDataDefiner() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean supportsExternalInitialization() {
			return false;
		}

		@Override
		public boolean producesVmAnonymousOrHiddenClasses() {
			return true;
		}

		@Override
		public <T> Class<T> defineClass(ClassDefinition classDefinition) {
			return defineClass(classDefinition, new Object());
		}

		public <T> Class<T> defineClass(ClassDefinition classDefinition, Object classData) {
			Lookup host = classDefinition.getHost();
			Utils.verifyHasClassLoader(host.lookupClass());
			byte[] bytes = classDefinition.getBytes();

			try {
				Lookup definedLookup = (Lookup) Holder.DEFINE_HIDDEN_CLASS_WITH_CLASS_DATA.invokeExact(
						host,
						bytes,
						classData,
						false,
						Holder.EMPTY_CLASS_OPTIONS);

				return cast(definedLookup);
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw ClassDefinerException.forClass(classDefinition.getName(), e);
			}
		}

		@SuppressWarnings("unchecked")
		private static <T> Class<T> cast(Lookup definedLookup) {
			return (Class<T>) definedLookup.lookupClass();
		}

		private static final class Holder {

			static final MethodHandle DEFINE_HIDDEN_CLASS_WITH_CLASS_DATA;

			static final Object EMPTY_CLASS_OPTIONS;

			static {
				try {
					EMPTY_CLASS_OPTIONS = Array.newInstance(Internals.OptionalClass.CLASS_OPTION.load(), 0);
					Class<?> classOptionArray = EMPTY_CLASS_OPTIONS.getClass();
					MethodType methodType = MethodType.methodType(Lookup.class, byte[].class, Object.class, boolean.class, classOptionArray);
					Lookup lookup = MethodHandles.lookup();
					MethodHandle defineHiddenClassWithClassData = lookup.findVirtual(Lookup.class, "defineHiddenClassWithClassData", methodType)
							.asFixedArity();
					DEFINE_HIDDEN_CLASS_WITH_CLASS_DATA = defineHiddenClassWithClassData
							.asType(defineHiddenClassWithClassData.type().changeParameterType(4, Object.class));
				} catch (Exception e) {
					throw new ExceptionInInitializerError(e);
				}
			}

		}

	}

	/**
	 * <p>
	 * Leverages <code>sun.misc.Unsafe</code> to create more lightweight classes. Some JVMs optimize VM-anonymous classes more aggressively
	 * than regular classes.
	 * See <a href="http://hg.openjdk.java.net/jdk-updates/jdk11u/file/default/src/hotspot/share/ci/ciField.cpp#l227">ciField.cpp</a>
	 * </p>
	 *
	 * <p>
	 * This is essentially a programmatic version of <code>-XX:+TrustFinalNonStaticFields</code> that does not require unlocking
	 * experimental JVM flags that affect the entire application in unknown ways. With the use of static final fields in dynamically
	 * generated classes, these optimizations might have become unnecessary. However, in Java 14 and older there is no support for hidden
	 * classes, therefore VM-anonymous classes are the preferred way to create ClassLoader-independent classes. Furthermore, it is
	 * impossible to deserialize such classes directly (without using a serialization proxy), due to the fact that they cannot be referred
	 * to by any valid name.
	 * <p>
	 */
	@ThreadSafe
	static final class VMAnonymousClassDefiner implements ClassDefiner {

		VMAnonymousClassDefiner() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean supportsExternalInitialization() {
			return true;
		}

		@Override
		public boolean producesVmAnonymousOrHiddenClasses() {
			return true;
		}

		@Override
		public <T> Class<T> defineClass(ClassDefinition classDefinition) {
			return defineClass(classDefinition, null);
		}

		public <T> Class<T> defineClass(ClassDefinition classDefinition, @Nullable Object[] constantPoolPatches) {
			try {
				byte[] bytes = classDefinition.getBytes();
				Class<?> hostClass = classDefinition.getHost().lookupClass();
				Utils.verifyHasClassLoader(hostClass);
				@SuppressWarnings("unchecked")
				Class<T> generated = (Class<T>) Holder.DEFINE_ANONYMOUS_CLASS.invokeExact(hostClass, bytes, constantPoolPatches);

				return generated;
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw ClassDefinerException.forClass(classDefinition.getName(), e);
			}
		}

		private static final class Holder {

			static final MethodHandle DEFINE_ANONYMOUS_CLASS = Internals.Unsafe.Method.DEFINE_ANONYMOUS_CLASS.find()
					.asFixedArity()
					.bindTo(Internals.Unsafe.getInstance());

		}

	}

	private static final class ClassDefinerException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public static ClassDefinerException forClass(String className, Throwable cause) {
			return new ClassDefinerException("Cannot define class: " + className, cause);
		}

		private ClassDefinerException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	private ClassDefiners() {
		throw new AssertionError("No instances");
	}

}
