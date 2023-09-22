package io.github.jdcmp.codegen.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Indicates that code, despite being public, is not part of the "public API", meaning it should not be used or otherwise
 * relied upon by outside internal development of the particular project. This may be the case if a type must be public for
 * technical reasons, yet provides no obvious functionality.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
		ElementType.TYPE,
		ElementType.CONSTRUCTOR,
		ElementType.METHOD,
		ElementType.FIELD,
})
public @interface NotPublicAPI {

	/**
	 * Describes the reason for placing the annotation at a certain location.
	 *
	 * @return The reason
	 */
	String reason();

}
