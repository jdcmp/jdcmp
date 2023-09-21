package io.github.jdcmp.codegen;

import io.github.jdcmp.api.documentation.Immutable;
import io.github.jdcmp.api.documentation.ThreadSafe;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

@ThreadSafe
interface ClassDefiner {

	boolean supportsExternalInitialization();

	boolean producesVmAnonymousOrHiddenClasses();

	<T> Class<T> defineClass(ClassDefinition classDefinition);

	@Immutable
	final class ClassDefinition {

		private final byte[] bytes;

		private final String name;

		private final Lookup host;

		public ClassDefinition(byte[] bytes, String name, Lookup host) {
			this.bytes = bytes.clone();
			this.name = Objects.requireNonNull(name);
			this.host = Objects.requireNonNull(host);
		}

		public byte[] getBytes() {
			return bytes.clone();
		}

		public String getName() {
			return name;
		}

		public Lookup getHost() {
			return host;
		}

	}
}
