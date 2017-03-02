/** 
 * Filename:    OOColumn.java
 * Description:  
 * @author:     kkmoving
 * @version:    1.0
 * Create at:   2014-1-14 下午12:42:16
 * 
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2014-1-14     kkmoving    1.0         1.0 Version
 */
package com.kkmoving.oosqlite;

import java.lang.reflect.Field;

public class OOColumn {
	public static final String TYPE_TEXT = "TEXT";
	public static final String TYPE_INTEGER = "INTEGER";
	public static final String TYPE_BYTES = "BYTES";
	public static final String TYPE_REAL = "REAL";
	
	private static final String TYPE_DEFAULT_VALUE_TEXT = "''";
	private static final String TYPE_DEFAULT_VALUE_INTEGER = "0";
	private static final String TYPE_DEFAULT_VALUE_BYTES = null;
	private static final String TYPE_DEFAULT_VALUE_FLOAT = "0.0";

	public enum ColumnType {
		TEXT(TYPE_TEXT, TYPE_DEFAULT_VALUE_TEXT),
		INTEGER(TYPE_INTEGER, TYPE_DEFAULT_VALUE_INTEGER),
		LONG(TYPE_INTEGER, TYPE_DEFAULT_VALUE_INTEGER),
		BOOLEAN(TYPE_INTEGER, TYPE_DEFAULT_VALUE_INTEGER),
		BYTES(TYPE_BYTES, TYPE_DEFAULT_VALUE_BYTES),
		FLOAT(TYPE_REAL, TYPE_DEFAULT_VALUE_FLOAT);
		
		public String mKey;
		public Object mDefaultValue;

		ColumnType(String key, Object value) {
			this.mKey = key;
			this.mDefaultValue = value;
		}
	}
	
	public String mName;
	ColumnType mColumnType;
	boolean mIndexing;
	boolean mPrimaryKey;
	int mTableIndex = -1;
	
	int mTargetVersion = -1;
	
	Field mField;
	
	public OOColumn(String name, ColumnType columnType) {
		this(name, columnType, false);
	}
	
	public OOColumn(String name, ColumnType columnType, boolean indexing) {
		this(name, columnType, indexing, false);
	}
	
	
	OOColumn(String name, ColumnType columnType, boolean indexing, boolean primaryKey) {
		mName = name;
		mColumnType = columnType;
		mIndexing = indexing;
		mPrimaryKey = primaryKey;
	}
	
	public static ColumnType parseType(Class type) {
		ColumnType columnType = null;
		if (type == null) {
			return columnType;
		}
		if (type.equals(String.class)) {
			columnType = ColumnType.TEXT;
		} else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
			columnType = ColumnType.BOOLEAN;
		} else if (type.equals(Integer.class) || type.equals(int.class)) {
			columnType = ColumnType.INTEGER;
		} else if (type.equals(Long.class) || type.equals(long.class)) {
			columnType = ColumnType.LONG;
		} else if (type.equals(byte[].class)) {
			columnType = ColumnType.BYTES;
		} else if (type.equals(Float.class) || type.equals(float.class)) {
			columnType = ColumnType.FLOAT;
		}
		return columnType;
	}
}
