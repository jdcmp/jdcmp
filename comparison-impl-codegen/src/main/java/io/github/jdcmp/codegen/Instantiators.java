package io.github.jdcmp.codegen;

import io.github.jdcmp.api.documentation.ThreadSafe;
import io.github.jdcmp.codegen.customization.AvailableInstantiator;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@ThreadSafe
final class Instantiators {

	private static final Logger LOGGER = Logger.getLogger(Instantiators.class.getName());

	static List<Instantiator> create(Collection<AvailableInstantiator> available) {
		ArrayList<Instantiator> instantiators = new ArrayList<>(4);
		create(available, instantiators, AvailableInstantiator.UNSAFE, UnsafeInstantiator::new);
		create(available, instantiators, AvailableInstantiator.REFLECTION_FACTORY, ReflectionFactoryInstantiator::new);
		create(available, instantiators, AvailableInstantiator.REFLECTION_FACTORY, ReflectionFactoryConstructorInstantiator::new);
		create(available, instantiators, AvailableInstantiator.CONSTRUCTOR, ConstructorInstantiator::new);

		return instantiators;
	}

	private static void create(
			Collection<AvailableInstantiator> available,
			ArrayList<Instantiator> list,
			AvailableInstantiator wanted,
			Callable<? extends Instantiator> factory) {
		if (available.contains(wanted)) {
			try {
				list.add(factory.call());
			} catch (Exception e) {
				LOGGER.log(Level.FINE, "Failed to load Instantiator: " + wanted, e);
			}
		}
	}

	static final class UnsafeInstantiator implements Instantiator {

		UnsafeInstantiator() {
			Utils.initializeClass(Holder.class);
		}

		@Override
		public boolean requiresConstructor() {
			return false;
		}

		@Override
		public boolean supportsVmAnonOrHiddenClasses() {
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

			static final MethodHandle ALLOCATE_INSTANCE;

			static {
				ALLOCATE_INSTANCE = Internals.Unsafe.Method.ALLOCATE_INSTANCE.find().bindTo(Internals.Unsafe.getInstance());
			}

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
		public boolean supportsVmAnonOrHiddenClasses() {
			return false;
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

			static final MethodHandle NEW_CONSTRUCTOR_FOR_SERIALIZATION;

			static {
				NEW_CONSTRUCTOR_FOR_SERIALIZATION = Internals.ReflectionFactory.Method.NEW_CONSTRUCTOR_FOR_SERIALIZATION
						.find()
						.bindTo(Internals.ReflectionFactory.getInstance());
			}

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
		public boolean supportsVmAnonOrHiddenClasses() {
			return false;
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

			static final MethodHandle NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR;

			static {
				NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR = Internals.ReflectionFactory.Method.NEW_CONSTRUCTOR_FOR_SERIALIZATION_CONSTRUCTOR
						.find()
						.bindTo(Internals.ReflectionFactory.getInstance());
			}

		}

	}

	static final class ConstructorInstantiator implements Instantiator {

		@Override
		public boolean requiresConstructor() {
			return true;
		}

		@Override
		public boolean supportsVmAnonOrHiddenClasses() {
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
