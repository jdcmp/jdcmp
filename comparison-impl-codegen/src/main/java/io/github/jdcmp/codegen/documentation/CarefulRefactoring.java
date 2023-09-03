package io.github.jdcmp.codegen.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates that additional care must be taken during refactoring. The code may be referenced from
 * areas unknown at compile time. Typical examples are generated code, configuration files, databases,
 * DTOs and JPA models.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
		ElementType.TYPE,
		ElementType.CONSTRUCTOR,
		ElementType.METHOD,
		ElementType.FIELD,
})
public @interface CarefulRefactoring {

	/**
	 * Describes the reason for placing the annotation at a certain location.
	 *
	 * @return The reason
	 */
	String reason();

}
