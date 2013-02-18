package com.groupagendas.groupagenda.metadata.impl;

import android.provider.BaseColumns;

import com.groupagendas.groupagenda.auto.AutoColorIconProvider;
import com.groupagendas.groupagenda.metadata.IMetaData;
import com.groupagendas.groupagenda.metadata.anno.Database;
import com.groupagendas.groupagenda.metadata.anno.Table;
import com.groupagendas.groupagenda.metadata.anno.TableColumn;
import com.groupagendas.groupagenda.metadata.storage.SQLiteType;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;

/**
 * This data used to be handled by the obsolete {@code AccountProvider} class.
 * 
 * @author Tadas
 */
@Database(name = "autoci.sqlite", version = 1, authority = AutoColorIconProvider.class)
public interface AutoColorIconMetaData extends IMetaData {
	@Table(name = "autocolor", databaseMetadata = AutoColorIconMetaData.class, bindTo = AutoColorItem.class)
	public interface AutoColor extends ITable {
		String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.autocolor_item";
		String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.autocolor_item";
		String DEFAULT_SORT_ORDER = "";
		
		@TableColumn(databaseType = SQLiteType.INT, databaseConstraints = "PRIMARY KEY",
				bindingGetterAlias = "getId", bindingSetterAlias = "setId")
		String C_ID = BaseColumns._ID;
		
		@TableColumn(databaseType = SQLiteType.STRING)
		String COLOR = "color";
		@TableColumn(databaseType = SQLiteType.STRING)
		String KEYWORD = "keyword";
		@TableColumn(databaseType = SQLiteType.STRING)
		String CONTEXT 	= "context";

		@TableColumn(databaseType = SQLiteType.INT, unbound = true)
		String NEED_UPDATE 	= "need_update";
	}
	
	@Table(name = "autoicon", databaseMetadata = AutoColorIconMetaData.class, bindTo = AutoIconItem.class)
	public interface AutoIcon extends ITable {
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.formula.autoicon_item";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.formula.autoicon_item";
		public static final String DEFAULT_SORT_ORDER = "";
		
		@TableColumn(databaseType = SQLiteType.INT, databaseConstraints = "PRIMARY KEY",
				bindingGetterAlias = "getId", bindingSetterAlias = "setId")
		String I_ID = BaseColumns._ID;
		
		@TableColumn(databaseType = SQLiteType.STRING)
		String ICON = "icon";
		@TableColumn(databaseType = SQLiteType.STRING)
		String KEYWORD = "keyword";
		@TableColumn(databaseType = SQLiteType.STRING)
		String CONTEXT = "context";

		@TableColumn(databaseType = SQLiteType.INT, unbound = true)
		String NEED_UPDATE = "need_update";
	}
}
