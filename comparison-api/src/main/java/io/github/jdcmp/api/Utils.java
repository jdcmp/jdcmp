package io.github.jdcmp.api;

import io.github.jdcmp.api.documentation.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ThreadSafe
final class Utils {

	private static final Set<Class<?>> FORBIDDEN_CLASSES;

	static {
		HashSet<Class<?>> set = new HashSet<>(2);
		set.add(void.class);
		set.add(Void.class);

		FORBIDDEN_CLASSES = Collections.unmodifiableSet(set);
	}

	public static <T> Class<T> verifyClassNotForbidden(Class<T> clazz) {
		Objects.requireNonNull(clazz);

		if (FORBIDDEN_CLASSES.contains(clazz)) {
			throw new IllegalArgumentException("Class is forbidden: " + clazz);
		}

		return clazz;
	}

	public static <T> T orNonNull(@Nullable T value, T alternative) {
		Objects.requireNonNull(alternative);

		return value != null ? value : alternative;
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

	private Utils() {
		throw new AssertionError("No instances");
	}

}
