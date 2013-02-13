package com.groupagendas.groupagenda.metadata.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import android.database.Cursor;

import com.groupagendas.groupagenda.metadata.TypeConversion;
import com.groupagendas.groupagenda.metadata.storage.JSONType;
import com.groupagendas.groupagenda.metadata.storage.SQLiteType;

/**
 * Defines a column in a table that binds to a variable of a specific object.
 * 
 * @author Tadas
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableColumn {
	/**
	 * Specifies the data type of this column in a SQLite database.
	 * 
	 * @return a type that can be obtained from a {@link Cursor} object
	 */
	SQLiteType databaseType();
	
	/**
	 * Specifies additional table column constraints, if any ({@code DEFAULT, NOT NULL, ...}).<BR>
	 * <BR>
	 * Returns no constraints (empty string) if not specified.
	 * 
	 * @return database constraints
	 */
	String databaseConstraints() default "";
	
	/**
	 * Specifies type conversions between values stored in the database and values used during runtime.<BR>
	 * <BR>
	 * Returns {@link TypeConversion#NONE} (a special value indicating that runtime type matches the stored type)
	 * if not specified.
	 * 
	 * @return type converter
	 */
	TypeConversion converter() default TypeConversion.NONE;
	
	/**
	 * Specifies a different getter method name for the bound object.<BR>
	 * <BR>
	 * Useful when dealing with when variables are named differently than column names.<BR>
	 * <BR>
	 * Returns an empty string if not specified. This means a getter/setter name will be
	 * automatically generated, using a default scheme:<BR>
	 * If column is named {@code myColumn_name}, then a generated setter would be {@code setMyColumn_name}.
	 * 
	 * @return
	 */
	String bindingGetterAlias() default "";
	String bindingSetterAlias() default "";
	JSONType jsonType() default JSONType.INFER;
	String jsonName() default "";
	//boolean jsonIgnoreMissing() default false;
	//boolean constant() default false;
	boolean excludeFromCV() default false;
	boolean unbound() default false;
}
