package io.github.jdcmp.codegen.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that additional care must be taken during refactoring, because elements may be
 * referenced from generated code (IDEs will usually not find all references).
 * See {@link CarefulRefactoring}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
		ElementType.TYPE,
		ElementType.CONSTRUCTOR,
		ElementType.METHOD,
		ElementType.FIELD,
})
@CarefulRefactoring(reason = "Called from generated code")
public @interface CalledFromGeneratedCode {

}
