package io.github.jdcmp.test;


import io.github.jdcmp.api.provider.ComparatorProvider;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.provider.Arguments;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class Providers {

	private static final Set<TestProvider> PROVIDERS = new LinkedHashSet<>();

	static {
		registerEnum(CodegenProvider.class);
	}

	public static <E extends Enum<E> & TestProvider> void registerEnum(Class<? extends E> enumClass) {
		for (E value : enumClass.getEnumConstants()) {
			register(value);
		}
	}

	public static void register(TestProvider provider) {
		JRE current = JRE.currentVersion();
		JRE min = provider.minJavaVersion();
		JRE max = provider.maxJavaVersion();

		if (current.compareTo(min) >= 0 && current.compareTo(max) <= 0) {
			PROVIDERS.add(provider);
		}
	}

	public static Stream<Arguments> providersToTest() {
		return PROVIDERS.stream().map(Supplier::get).map(Arguments::of);
	}

	public interface TestProvider extends Supplier<ComparatorProvider> {

		JRE minJavaVersion();

		JRE maxJavaVersion();

	}

	private Providers() {
		throw new AssertionError("No instances");
	}

}
