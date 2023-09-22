package io.github.jdcmp.codegen;


import io.github.jdcmp.api.documentation.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ThreadSafe
final class Utils {

	private static final @Nullable MethodHandle LOOKUP_ACCESS_CLASS = Internals.Lookup.Method.ACCESS_CLASS.tryFind().orElse(null);

	public static void verifyLookupCanAccess(Lookup lookup, Class<?> classToBeAccessed) throws IllegalAccessException {
		MethodHandle accessClass = LOOKUP_ACCESS_CLASS;

		if (accessClass != null) {
			ThrowableRunnable.run(() -> {
				Class<?> ignored = (Class<?>) accessClass.invokeExact(lookup, classToBeAccessed);
			});
			return;
		}

		if (lookup.in(classToBeAccessed).lookupModes() == 0) {
			throw new IllegalAccessException();
		}
	}

	public static ClassLoader verifyHasClassLoader(Class<?> clazz) {
		ClassLoader classLoader = clazz.getClassLoader();

		if (classLoader == null) {
			throw new IllegalArgumentException("Class has no ClassLoader: " + clazz);
		}

		return classLoader;
	}

	public static <T> T verify(T value, Predicate<? super T> predicate, Supplier<String> message) {
		if (!predicate.test(value)) {
			throw new IllegalArgumentException(message.get());
		}

		return value;
	}

	public static <T extends Throwable> T chainThrowables(@Nullable T chain, T e) {
		if (chain == null) {
			return Objects.requireNonNull(e);
		}

		chain.addSuppressed(e);

		return chain;
	}

	public static <T> @Nullable T or(@Nullable T first, @Nullable T alternative) {
		return first == null ? alternative : first;
	}

	public static <T> boolean isEmpty(@Nullable T[] array) {
		return array == null || array.length == 0;
	}

	public static boolean isEmpty(@Nullable Iterable<?> iterable) {
		return iterable == null || !iterable.iterator().hasNext();
	}

	public static boolean isNotEmpty(@Nullable Iterable<?> iterable) {
		return !isEmpty(iterable);
	}

	public static <E extends Enum<E>> Set<E> immutableEnumSet(Class<E> enumClass) {
		return Collections.unmodifiableSet(EnumSet.allOf(enumClass));
	}

	@SafeVarargs
	public static <E extends Enum<E>> @Nullable Set<E> immutableEnumSetOrNull(@Nullable E... values) {
		if (isEmpty(values)) {
			return null;
		}

		return Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(values)));
	}

	public static <T> ArrayList<T> arrayList(Iterable<? extends T> iterable) throws NullPointerException {
		Objects.requireNonNull(iterable);

		if (iterable instanceof Collection) {
			Collection<? extends T> collection = (Collection<? extends T>) iterable;
			ArrayList<T> list = new ArrayList<>(collection.size());
			for (T value : collection) {
				list.add(Objects.requireNonNull(value));
			}

			return list;
		}

		ArrayList<T> list = new ArrayList<>();
		for (T value : iterable) {
			list.add(Objects.requireNonNull(value));
		}

		return list;
	}

	public static <T> List<T> immutableArrayList(Iterable<? extends T> iterable) throws NullPointerException {
		return Collections.unmodifiableList(arrayList(iterable));
	}

	public static <T> List<T> immutableArrayListNonEmpty(Iterable<? extends T> iterable) throws NullPointerException, IllegalArgumentException {
		List<T> list = immutableArrayList(iterable);

		if (list.isEmpty()) {
			throw new IllegalArgumentException("List must not be empty");
		}

		return list;
	}

	@SafeVarargs
	public static <T> List<T> nonEmptyArrayListOfNonNulls(@Nullable T... elements) throws IllegalArgumentException {
		if (elements == null) {
			return Collections.emptyList();
		}

		ArrayList<T> list = new ArrayList<>(elements.length);
		for (T element : elements) {
			if (element != null) {
				list.add(element);
			}
		}

		if (list.isEmpty()) {
			throw new IllegalArgumentException("List must not be empty");
		}

		return list;
	}

	public static void initializeClass(Class<?> clazz) {
		ThrowableRunnable.run(() -> initializeClassChecked(clazz));
	}

	private static void initializeClassChecked(Class<?> clazz) throws Throwable {
		Class.forName(clazz.getName(), true, clazz.getClassLoader());
	}

	public interface ThrowableRunnable extends Runnable {

		static void run(ThrowableRunnable runnable) {
			runnable.run();
		}

		void runChecked() throws Throwable;

		@Override
		default void run() {
			try {
				runChecked();
			} catch (@SuppressWarnings("removal") RuntimeException | ThreadDeath e) {
				throw e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public interface ThrowableConsumer<T> extends Consumer<T> {

		void acceptChecked(T t) throws Throwable;

		@Override
		default void accept(T t) {
			try {
				acceptChecked(t);
			} catch (@SuppressWarnings("removal") RuntimeException | ThreadDeath e) {
				throw e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public interface ThrowableFunction<T, R> extends Function<T, R> {

		R applyChecked(T t) throws Throwable;

		@Override
		default R apply(T t) {
			try {
				return applyChecked(t);
			} catch (@SuppressWarnings("removal") RuntimeException | ThreadDeath e) {
				throw e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Utils() {
		throw new AssertionError("No instances");
	}

}
