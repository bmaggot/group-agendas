package com.groupagendas.groupagenda.metadata;

import android.provider.BaseColumns;

/**
 * Each metadata class in this package must implement this interface for correct operation.
 * 
 * @author Tadas
 */
public interface IMetaData {
	// marker interface
	String CREATE_TABLE = "CREATE TABLE %name% (%struct%)";
	
	/**
	 * Each table-defining class must implement this interface for correct operation.<BR>
	 * Moreover, each extending interface <b>must</b> be defined in an enclosing {@link IMetaData} class.
	 * 
	 * @author Tadas
	 */
	public interface ITable extends BaseColumns {
		// marker interface
	}
}
