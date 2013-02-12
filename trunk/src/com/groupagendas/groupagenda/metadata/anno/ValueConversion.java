package com.groupagendas.groupagenda.metadata.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a method to be used for database value to runtime value conversion.
 * 
 * @author Tadas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValueConversion {
	/**
	 * Whether this method converts a stored type to runtime type or vice versa?
	 * 
	 * @return {@code true}, if this method converts a DB/JSON type to a runtime type.
	 */
	boolean toRuntimeType();
	
	/**
	 * Whether this method targets JSON or DB?
	 * 
	 * @return {@code true}, if this method should be used in JSON context only;
	 * 		{@code false}, if this method is for DB use only.
	 */
	boolean forJSON();
}
