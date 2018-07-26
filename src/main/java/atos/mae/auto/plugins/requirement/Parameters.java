package atos.mae.auto.plugins.requirement;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Enable --exportaction command line to take method parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) //can use in method only.
public @interface Parameters {
	/**
	 * List of parameters needed.
	 * @return Parameter's list as String
	 */
	public String value();
}
