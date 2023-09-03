package io.github.jdcmp.api.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Indicates that an implementation is immutable. If placed on an interface or abstract class, all implementations must be
 * immutable as well.</p>
 *
 * <p>This annotation may be replaced by a suitable 3rd party library in the future.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
		ElementType.TYPE,
})
public @interface Immutable {

}
