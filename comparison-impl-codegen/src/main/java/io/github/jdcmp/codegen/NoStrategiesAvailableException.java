package io.github.jdcmp.codegen;

/**
 * Indicates that the combination of configuration and JVM/platform did not yield any usable
 * strategies to generate a comparator.
 */
final class NoStrategiesAvailableException extends IllegalStateException {

	public static NoStrategiesAvailableException of(ImplSpec implSpec) {
		String message = "Cannot generate comparator, no strategies are available using the given" +
				" configuration." +
				" ClassDefiners=" + implSpec.getClassDefiners().all() +
				", Instantiators=" + implSpec.getInstantiators() +
				", InitializationMode=" + implSpec.getInitializationMode();

		return new NoStrategiesAvailableException(message);
	}

	private NoStrategiesAvailableException(String message) {
		super(message);
	}

}
