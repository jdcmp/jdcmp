package io.github.jdcmp.codegen;

import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.codegen.Internals.ReflectionFactory;
import io.github.jdcmp.codegen.Internals.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;

@ThreadSafe
final class Instantiators {

	static final class UnsafeInstantiator implements Instantiator {

		UnsafeInstantiator() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean requiresConstructor() {
			return false;
		}

		@Override
		public boolean supports(ClassDefiner classDefiner) {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T newInstance(Class<? extends T> clazz) {
			try {
				Object instance = (Object) Holder.ALLOCATE_INSTANCE.invokeExact(clazz);

				return (T) instance;
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw InstantiationException.forClass(clazz, e);
			}
		}

		private static final class Holder {

			static final MethodHandle ALLOCATE_INSTANCE
					= Unsafe.Method.ALLOCATE_INSTANCE.find().bindTo(Unsafe.getInstance());

		}

	}

	static final class ReflectionFactoryInstantiator implements Instantiator {

		ReflectionFactoryInstantiator() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean requiresConstructor() {
			return false;
		}

		@Override
		public boolean supports(ClassDefiner classDefiner) {
			return !classDefiner.producesVmAnonymousOrHiddenClasses();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T newInstance(Class<? extends T> clazz) {
			Constructor<? extends T> constructor;
			try {
				MethodHandle method = Holder.NEW_CONSTRUCTOR_FOR_SERIALIZATION;
				constructor = (Constructor<? extends T>) method.invokeExact(clazz);
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException("Failed to create serialization constructor for class: " + clazz, e);
			}

			if (constructor == null) {
				throw new RuntimeException("The constructor returned by newConstructorForSerialization is null.");
			}

			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw InstantiationException.forClass(clazz, e);
			}
		}

		private static final class Holder {

			static final MethodHandle NEW_CONSTRUCTOR_FOR_SERIALIZATION =
					ReflectionFactory.Method.NEW_CONSTRUCTOR_FOR_SERIALIZATION
							.find().bindTo(ReflectionFactory.getInstance());

		}

	}

	static final class ReflectionFactoryConstructorInstantiator implements Instantiator {

		ReflectionFactoryConstructorInstantiator() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean requiresConstructor() {
			return false;
		}

		@Override
		public boolean supports(ClassDefiner classDefiner) {
			return !classDefiner.producesVmAnonymousOrHiddenClasses();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T newInstance(Class<? extends T> clazz) {
			Constructor<? extends T> constructor;
			try {
				Constructor<Object> objectConstructor = Object.class.getDeclaredConstructor();
				MethodHandle method = Holder.NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR;
				constructor = (Constructor<? extends T>) method.invokeExact(clazz, objectConstructor);
			} catch (@SuppressWarnings("removal") ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw new RuntimeException("Failed to create serialization constructor for class: " + clazz, e);
			}

			if (constructor == null) {
				throw new RuntimeException("The constructor returned by newConstructorForSerialization is null.");
			}

			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw InstantiationException.forClass(clazz, e);
			}
		}

		private static final class Holder {

			static final MethodHandle NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR =
					ReflectionFactory.Method.NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR
							.find().bindTo(ReflectionFactory.getInstance());

		}

	}

	static final class ConstructorInstantiator implements Instantiator {

		@Override
		public boolean requiresConstructor() {
			return true;
		}

		@Override
		public boolean supports(ClassDefiner classDefiner) {
			return true;
		}

		@Override
		public <T> T newInstance(Class<? extends T> clazz) {
			Constructor<? extends T> constructor;
			try {
				constructor = clazz.getDeclaredConstructor();
			} catch (Exception e) {
				throw new RuntimeException("Failed to obtain constructor for class: " + clazz, e);
			}

			try {
				constructor.setAccessible(true);
			} catch (Exception e) {
				throw new RuntimeException("Constructor is inaccessible: " + constructor, e);
			}

			try {
				return constructor.newInstance();
			} catch (Exception e) {
				throw InstantiationException.forClass(clazz, e);
			}
		}

	}

	static final class InstantiationException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public static InstantiationException forClass(Class<?> clazz, Throwable cause) {
			return new InstantiationException("Cannot create instance of class: " + clazz, cause);
		}

		private InstantiationException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	private Instantiators() {
		throw new AssertionError("No instances");
	}

}
