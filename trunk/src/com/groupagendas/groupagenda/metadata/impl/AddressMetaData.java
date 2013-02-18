package com.groupagendas.groupagenda.metadata.impl;

import android.provider.BaseColumns;

import com.groupagendas.groupagenda.address.Address;
import com.groupagendas.groupagenda.address.AddressProvider;
import com.groupagendas.groupagenda.metadata.IMetaData;
import com.groupagendas.groupagenda.metadata.TypeConversion;
import com.groupagendas.groupagenda.metadata.anno.Database;
import com.groupagendas.groupagenda.metadata.anno.Table;
import com.groupagendas.groupagenda.metadata.anno.TableColumn;
import com.groupagendas.groupagenda.metadata.storage.JSONType;
import com.groupagendas.groupagenda.metadata.storage.SQLiteType;

/**
 * This interface stores information about the address database and a single table inside it.
 * 
 * @author Tadas
 */
@Database(name = "addresses.sqlite", version = 1, authority = AddressProvider.class)
public interface AddressMetaData extends IMetaData {
	/**
	 * This interface stores information about the only table in the address database.
	 * 
	 * @author Tadas
	 */
	@Table(name = "addresses", databaseMetadata = AddressMetaData.class, bindTo = Address.class)
	public interface AddressTable extends ITable {
		String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.address_item";
		String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.address_item";

		@TableColumn(databaseType = SQLiteType.INT, databaseConstraints = "PRIMARY KEY AUTOINCREMENT",
				excludeFromCV = true, jsonType = JSONType.TRANSIENT,
				bindingGetterAlias = "getIdInternal", bindingSetterAlias = "setIdInternal")
		String _ID = BaseColumns._ID;
		@TableColumn(databaseType = SQLiteType.INT)
		String A_ID = "id";
		@TableColumn(databaseType = SQLiteType.INT)
		String USER_ID = "user_id";
		@TableColumn(databaseType = SQLiteType.STRING)
		String TITLE = "title";
		@TableColumn(databaseType = SQLiteType.STRING)
		String STREET = "street";
		@TableColumn(databaseType = SQLiteType.STRING)
		String CITY = "city";
		@TableColumn(databaseType = SQLiteType.STRING)
		String ZIP = "zip";
		@TableColumn(databaseType = SQLiteType.STRING)
		String STATE = "state";
		@TableColumn(databaseType = SQLiteType.STRING)
		String COUNTRY = "country";
		@TableColumn(databaseType = SQLiteType.STRING)
		String TIMEZONE = "timezone";
		@TableColumn(databaseType = SQLiteType.STRING)
		String COUNTRY_NAME = "country_name";
		@TableColumn(databaseType = SQLiteType.INT, databaseConstraints = "DEFAULT 0",
				converter = TypeConversion.NUMERIC_BOOLEAN,
				jsonType = JSONType.TRANSIENT,
				bindingGetterAlias = "isUploadedToServer", bindingSetterAlias = "setUploadedToServer")
		String UPLOADED_SUCCESSFULLY = "uploaded";

		String DEFAULT_SORT_ORDER = TITLE + " ASC";
	}
}
