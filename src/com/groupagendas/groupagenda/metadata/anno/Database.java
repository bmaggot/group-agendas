package com.groupagendas.groupagenda.metadata.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import android.content.ContentProvider;

/**
 * Defines information about the database.
 * 
 * @author Tadas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Database {
	String name();
	int version();
	Class<? extends ContentProvider> authority();
}
