package io.github.jdcmp.test;

import io.github.jdcmp.api.provider.ComparatorProvider;
import io.github.jdcmp.codegen.customization.AvailableClassDefiner;
import io.github.jdcmp.codegen.customization.AvailableInitializationMode;
import org.junit.jupiter.api.condition.JRE;

import java.lang.invoke.MethodHandles;

enum CodegenProvider implements Providers.TestProvider {

	DEFAULT {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_8;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			return new io.github.jdcmp.codegen.CodegenProvider();
		}
	},
	GENERATE_BRIDGE_METHODS {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_8;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			io.github.jdcmp.codegen.CodegenProvider provider = io.github.jdcmp.codegen.CodegenProvider.of(lookup);
			provider.setGenerateBridgeMethods(true);

			return provider;
		}
	},
	INITIALIZATION_MODE_EXTERNAL {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_8;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.EXTERNAL);

			return provider;
		}
	},
	INITIALIZATION_MODE_STATIC_INITIALIZER_BRIDGE {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_8;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER, AvailableClassDefiner.LOOKUP);

			return provider;
		}
	},
	CLASS_DEFINER_VM_ANONYMOUS {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_8;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_16;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.VM_ANONYMOUS);

			return provider;
		}
	},
	CLASS_DEFINER_LOOKUP_HIDDEN_WITH_CLASS_DATA {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_16;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN_CLASS_DATA);

			return provider;
		}
	},
	CLASS_DEFINER_LOOKUP_HIDDEN {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_15;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP_HIDDEN);

			return provider;
		}
	},
	CLASS_DEFINER_LOOKUP {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_9;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_22;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.LOOKUP);

			return provider;
		}
	},
	CLASS_DEFINER_CLASS_LOADER {
		@Override
		public JRE minJavaVersion() {
			return JRE.JAVA_8;
		}

		@Override
		public JRE maxJavaVersion() {
			return JRE.JAVA_16;
		}

		@Override
		public ComparatorProvider get() {
			io.github.jdcmp.codegen.CodegenProvider provider = new io.github.jdcmp.codegen.CodegenProvider();
			provider.setInitializationMode(AvailableInitializationMode.STATIC_INITIALIZER);
			provider.setClassDefiners(AvailableClassDefiner.CLASS_LOADER);

			return provider;
		}
	};

	@Override
	abstract public ComparatorProvider get();

}
