package com.groupagendas.groupagenda.metadata.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.groupagendas.groupagenda.metadata.IMetaData;

/**
 * Defines a table inside a database.
 * 
 * @author Tadas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
	String name();
	Class<? extends IMetaData> databaseMetadata();
	Class<?> bindTo();
}
