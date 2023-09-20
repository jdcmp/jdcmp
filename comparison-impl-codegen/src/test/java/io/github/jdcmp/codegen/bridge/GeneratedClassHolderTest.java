package io.github.jdcmp.codegen.bridge;

import io.github.jdcmp.codegen.CodegenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

public class GeneratedClassHolderTest {

	static final Lookup LOOKUP = MethodHandles.lookup();

	@Test
	void lookup_wrongLookupClass() {
		Assertions.assertThrows(IllegalArgumentException.class, useLookup(LOOKUP));
	}

	@Test
	void lookup_underprivilegedLookup() {
		Assertions.assertThrows(IllegalArgumentException.class, useLookup(LOOKUP.in(CodegenProvider.class)));
		Assertions.assertThrows(IllegalArgumentException.class, useLookup(MethodHandles.publicLookup()));
	}

	private static Executable useLookup(Lookup lookup) {
		return () -> GeneratedClassHolder.lookup(lookup);
	}

}
