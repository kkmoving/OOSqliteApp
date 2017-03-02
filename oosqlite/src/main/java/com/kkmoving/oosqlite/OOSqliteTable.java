/** 
 * Filename:    OOSqliteTable.java
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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OOSqliteTable {

	public static final String IDX_PREFIX = "idx_";

	protected String mTableName;

	protected List<OOColumn> mColumnList;
	protected int mDbColumnCnt;

	protected OOSqliteConventer mConventer;

	protected OODatabase mDatabase;

	private boolean mTableIndexSetted;
	
	private OOTableListener mListener;
	
	private boolean mReady;
	private SQLiteDatabase mUsableDb;
	
	Class mCls;

    private SparseArray<OOColumn> mTagArray;

	public OOSqliteTable(Class cls, String tblName, OOTableListener listener) {
		mCls = cls;
		
		mTableName = tblName;

        mListener = listener;
	}

    void init(OODatabase database) {
        mDatabase = database;

        mColumnList = new ArrayList<OOColumn>();
        List<OOColumn> dbColumnList = OOSqliteEntity.buildDbColumns();
        mDbColumnCnt = dbColumnList.size();
        mColumnList.addAll(dbColumnList);

        mTagArray = new SparseArray<OOColumn>();
        List<OOColumn> columnList = fetchPersistentFields(mCls);
        if (columnList != null) {
            mColumnList.addAll(columnList);
        }

        mConventer = new OOSqliteConventer() {

            @Override
            public Object convertToDb(OOColumn columnDef, OOSqliteEntity entity) {
                return convertFromEntityToDb(columnDef, entity);
            }

            @Override
            public OOSqliteEntity convertFromDb(Class cls, Map<OOColumn, Object> colValueMap) {
                return convertFromDbToEntity(cls, colValueMap);
            }
        };

        mReady = false;
        mUsableDb = null;
    }

    private List<OOColumn> fetchPersistentFields(Class cls) {
        List<OOColumn> list = null;
        Field[] fields = cls.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            list = new ArrayList<OOColumn>();
            for (Field field : fields) {
                if (shouldPersistent(field)) {
                    String name = "_" + field.getName();
                    Class type = field.getType();
                    OOColumn.ColumnType columnType = OOColumn.parseType(type);
                    boolean indexing = field.getAnnotation(Indexing.class) != null;

                    OOColumn columnDef = new OOColumn(name, columnType, indexing);

                    TargetVersion targetVersion = field.getAnnotation(TargetVersion.class);
                    if (targetVersion != null) {
                        columnDef.mTargetVersion = targetVersion.value();
                    }
                    columnDef.mField = field;

                    ColumnTag columnTag = field.getAnnotation(ColumnTag.class);
                    if (columnTag != null) {
                        mTagArray.put(columnTag.value(), columnDef);
                    }

                    if (columnDef.mTargetVersion == -1 || mDatabase.getVersion() >= columnDef.mTargetVersion) {
                        list.add(columnDef);
                    }
                }
            }
        }
        return list;
    }

    private boolean shouldPersistent(Field field) {
        int modifier = field.getModifiers();
        boolean isStatic = Modifier.isStatic(modifier);
        if (isStatic) {
            return false;
        }
        Transient trans = field.getAnnotation(Transient.class);
        return trans == null;
    }

    public OOColumn getColumn(int columnTag) {
        return mTagArray.get(columnTag);
    }

    protected void shouldAddColumns(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (mColumnList != null) {
            for (OOColumn columnDef : mColumnList) {
                if (columnDef.mTargetVersion > oldVersion) {
                    addColumn(db, columnDef);
                }
            }
        }
    }

    private static OOSqliteEntity convertFromDbToEntity(Class cls, Map<OOColumn, Object> apColValueMap) {
        OOSqliteEntity entity = null;
        try {
            Constructor<?> constructor = cls.getDeclaredConstructor();
            constructor.setAccessible(true);
            entity = (OOSqliteEntity) constructor.newInstance();
            Set<Map.Entry<OOColumn, Object>> colSet = apColValueMap.entrySet();
            for (Map.Entry<OOColumn, Object> entry : colSet) {
                OOColumn columnDef = entry.getKey();
                Object value = apColValueMap.get(columnDef);

                Field field = columnDef.mField;
                if (field != null) {
                    field.setAccessible(true);
                    field.set(entity, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    private static Object convertFromEntityToDb(OOColumn columnDef, OOSqliteEntity entity) {
        Object value = null;
        Field field = columnDef.mField;
        if (field != null) {
            field.setAccessible(true);
            try {
                value = field.get(entity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
	
	public static String nullSelection(OOColumn columnDef) {
		return columnDef.mName + " is null";
	}
	
	public static String notNullSelection(OOColumn columnDef) {
		String selection = columnDef.mName + " is not null";
		return selection;
	}

	public static String equalSelection(OOColumn columnDef, Object value) {
		String selection = null;
		if (columnDef.mColumnType == OOColumn.ColumnType.INTEGER) {
			selection = columnDef.mName + "=" + ((Integer) value).intValue();
		} else if (columnDef.mColumnType == OOColumn.ColumnType.LONG) {
			selection = columnDef.mName + "=" + ((Long) value).longValue();
		} else if (columnDef.mColumnType == OOColumn.ColumnType.TEXT) {
			selection = columnDef.mName + "='" + ((String) value) + "'";
		} else if (columnDef.mColumnType == OOColumn.ColumnType.BOOLEAN) {
			if (((Boolean) value).booleanValue()) {
				selection = columnDef.mName + ">0";
			} else {
				selection = columnDef.mName + "<0";
			}
		}
		return selection;
	}
	
	public static String likeSelection(OOColumn columnDef, Object value, boolean leftLike,
			boolean rightLike) {
		String selection = null;
		if (columnDef.mColumnType == OOColumn.ColumnType.INTEGER) {
			selection = columnDef.mName + "=" + ((Integer) value).intValue();
		} else if (columnDef.mColumnType == OOColumn.ColumnType.LONG) {
			selection = columnDef.mName + "=" + ((Long) value).longValue();
		} else if (columnDef.mColumnType == OOColumn.ColumnType.TEXT) {
			value = ((String) value).replace("'", "''");
			if (!leftLike && !rightLike) {
				selection = columnDef.mName + "='" + value + "'";
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append(columnDef.mName);
				sb.append(" LIKE '");
				sb.append(leftLike ? "%" : "");
				sb.append(value);
				sb.append(rightLike ? "%" : "");
				sb.append("'");
				selection = sb.toString();
			}
		} else if (columnDef.mColumnType == OOColumn.ColumnType.BOOLEAN) {
			if (((Boolean) value).booleanValue()) {
				selection = columnDef.mName + ">0";
			} else {
				selection = columnDef.mName + "<0";
			}
		}
		return selection;
	}

	private String columnFiledSql(OOColumn columnDef) {
		StringBuilder sb = new StringBuilder();
		sb.append(columnDef.mName + " " + columnDef.mColumnType.mKey);
		if (columnDef.mPrimaryKey) {
			sb.append(" PRIMARY KEY AUTOINCREMENT");
		} else {
			sb.append(" DEFAULT " + columnDef.mColumnType.mDefaultValue);
		}
		return sb.toString();
	}

	private String columnIndexSql(OOColumn columnDef) {
		return "CREATE INDEX IF NOT EXISTS " + indexName(columnDef) + " ON " + mTableName + " ("
				+ columnDef.mName + ");";
	}

	private String indexName(OOColumn columnDef) {
		return IDX_PREFIX + mTableName + columnDef.mName;
	}

	public void createTable(SQLiteDatabase db) {
		if (mColumnList == null || mColumnList.size() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS " + mTableName + " (");
		for (int index = 0; index < mColumnList.size(); index++) {
			OOColumn columnDef = mColumnList.get(index);
			String columnSql = columnFiledSql(columnDef);
			if (index > 0) {
				sb.append(", ");
			}
			sb.append(columnSql);
		}
		sb.append(");");
		String sql = sb.toString();
		db.execSQL(sql);

		for (OOColumn columnDef : mColumnList) {
			if (columnDef.mIndexing) {
				sql = columnIndexSql(columnDef);
				db.execSQL(sql);
			}
		}
	}
	
	public void addColumn(SQLiteDatabase db, OOColumn columnDef) {
		String columnSql = columnFiledSql(columnDef);
		
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ").append(mTableName).append(" ADD COLUMN ").append(columnSql);
		String sql = sb.toString();
		
		db.execSQL(sql);
	}

	protected void onCreate(final SQLiteDatabase db) {
		mUsableDb = db;
		
		createTable(db);

        if (mListener != null) {
            mListener.onCreate();
        }
		
		mUsableDb = null;
	}
	
	protected void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		mUsableDb = db;
		
		createTable(db);
		
		shouldAddColumns(db, oldVersion, newVersion);
		
		if (mListener != null) {
			mListener.onUpgrade(oldVersion, newVersion);
		}
		mUsableDb = null;
	}
	
	protected void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		mUsableDb = db;
		
		createTable(db);
		
		if (mListener != null) {
			mListener.onDowngrade(oldVersion, newVersion);
		}
		mUsableDb = null;
	}
	
	protected void onOpen(final SQLiteDatabase db) {
		if (!mReady) {
			mUsableDb = db;
			if (mListener != null) {
				mListener.onReady();
			}
			mUsableDb = null;
			mReady = true;
		}
	}
	
	public OOSqliteEntity insertFetch(OOSqliteEntity entity) {
		long id = insert(entity);
		String idSelection = OOSqliteEntity.idWhereClause(id);
		return querySingle(idSelection);
	}

	public long insert(OOSqliteEntity entity) {
		if (!dbUsable()) {
			return -1;
		}

		if (getUsableDb() == null) {
			return -1;
		}

		ContentValues values = convertToContentValues(entity);

		long id = getUsableDb().insert(mTableName, null, values);
		return id;
	}
	
	public int insertList(List<? extends OOSqliteEntity> list) {
		if (!dbUsable() || list == null) {
			return -1;
		}
		getUsableDb().beginTransaction();
		
		try {
			int n = list.size();
			for (int i = 0; i < n; i++) {
				insert((list.get(i)));
			}
			getUsableDb().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			getUsableDb().endTransaction();
		}
		return list.size();
	}
	
	private boolean dbUsable() {
		if (mReady) {
			return true;
		}
		if (mUsableDb != null || mDatabase.mOpenHelper.getWritableDatabase() != null) {
			return true;
		}
		return false;
	}
	
	private SQLiteDatabase getUsableDb() {
		if (!mReady) {
			return mUsableDb;
		}
		return mDatabase.mOpenHelper.getWritableDatabase();
	}

	private ContentValues convertToContentValues(OOSqliteEntity entity) {
		ContentValues values = OOSqliteEntity.convertToContentValues(entity,
                mColumnList.subList(0, mDbColumnCnt));
		for (int index = mDbColumnCnt; index < mColumnList.size(); index++) {
			OOColumn columnDef = mColumnList.get(index);
			Object value = mConventer.convertToDb(columnDef, entity);
			if (columnDef == null) {
				return values;
			}
			if (value == null) {
				values.putNull(columnDef.mName);
			} else if (columnDef.mColumnType == OOColumn.ColumnType.INTEGER) {
				values.put(columnDef.mName, (Integer) value);
			} else if (columnDef.mColumnType == OOColumn.ColumnType.LONG) {
				values.put(columnDef.mName, (Long) value);
			} else if (columnDef.mColumnType == OOColumn.ColumnType.TEXT) {
				values.put(columnDef.mName, (String) value);
			} else if (columnDef.mColumnType == OOColumn.ColumnType.BOOLEAN) {
				values.put(columnDef.mName, (Boolean) value);
			} else if (columnDef.mColumnType == OOColumn.ColumnType.BYTES) {
				values.put(columnDef.mName, (byte[]) value);
			} else if (columnDef.mColumnType == OOColumn.ColumnType.FLOAT) {
				values.put(columnDef.mName, (Float) value);
			}
		}
		return values;
	}
	
	public int delete(OOSqliteEntity entity) {
		if (entity.mDbId == -1) {
			throw new OOSqliteException("Could not update detattch entity!");
		}
		String whereClause = entity.idWhereClause();
		return delete(whereClause);
	}

	public int delete(String whereClause) {
		if (!dbUsable()) {
			return -1;
		}
		return getUsableDb().delete(mTableName, whereClause, null);
	}
	
	public OOSqliteEntity querySingle(String singleSelection) {
		List list = query(singleSelection);
		if (list != null && list.size() > 0) {
			return (OOSqliteEntity) list.get(0);
		}
		return null;
	}

	public List query(String selection) {
		return query(selection, null, null);
	}
	
	public List query(String selection, String sortOrder, String limit) {
		List list = null;
		try {
			if (!dbUsable()) {
				return null;
			}
			Cursor cursor = getUsableDb().query(mTableName, null, selection,
					null, null, null, sortOrder, limit);
			list = convertCursor(cursor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	protected int countQuery(String selection) {
		int count = 0;
		if (!dbUsable()) {
			return count;
		}
		String sql = "select count(*) from " + mTableName + " where " + selection;
		Cursor countCursor = getUsableDb().rawQuery(sql, null);
		if (countCursor != null) {
			countCursor.moveToFirst();
			count = countCursor.getInt(0);
			countCursor.close();
		}
		return count;
	}

	private List<OOSqliteEntity> convertCursor(Cursor cursor) {
		if (cursor == null) {
			return null;
		}
		setColumnIndex(cursor);

		List<OOSqliteEntity> list = new ArrayList<OOSqliteEntity>();
		while (cursor.moveToNext()) {
			OOSqliteEntity entity = convertOneCursor(cursor);
			list.add(entity);
		}
		cursor.close();
		return list;
	}

	private OOSqliteEntity convertOneCursor(Cursor cursor) {
		Map<OOColumn, Object> dbColValueMap = new HashMap<OOColumn, Object>();
		Map<OOColumn, Object> apColValueMap = new HashMap<OOColumn, Object>();
		for (int index = 0; index < mDbColumnCnt; index++) {
			OOColumn columnDef = mColumnList.get(index);
			Object value = parseCursor(cursor, columnDef);
			dbColValueMap.put(columnDef, value);
		}
		for (int index = mDbColumnCnt; index < mColumnList.size(); index++) {
			OOColumn columnDef = mColumnList.get(index);
			Object value = parseCursor(cursor, columnDef);
			apColValueMap.put(columnDef, value);
		}
		OOSqliteEntity entity = mConventer.convertFromDb(mCls, apColValueMap);
		OOSqliteEntity.convertFromDb(entity, dbColValueMap);
		return entity;
	}
	
	private Object parseCursor(Cursor cursor, OOColumn columnDef) {
		Object value = null;
		if (columnDef.mColumnType == OOColumn.ColumnType.INTEGER) {
			value = cursor.getInt(columnDef.mTableIndex);
		} else if (columnDef.mColumnType == OOColumn.ColumnType.LONG) {
			value = cursor.getLong(columnDef.mTableIndex);
		} else if (columnDef.mColumnType == OOColumn.ColumnType.TEXT) {
			value = cursor.getString(columnDef.mTableIndex);
		} else if (columnDef.mColumnType == OOColumn.ColumnType.BOOLEAN) {
			value = cursor.getInt(columnDef.mTableIndex) > 0;
		} else if (columnDef.mColumnType == OOColumn.ColumnType.BYTES) {
			value = cursor.getBlob(columnDef.mTableIndex);
		} else if (columnDef.mColumnType == OOColumn.ColumnType.FLOAT) {
			value = cursor.getFloat(columnDef.mTableIndex);
		}
		return value;
	}

	private void setColumnIndex(Cursor cursor) {
		if (mTableIndexSetted) {
			return;
		}
		for (OOColumn columnDef : mColumnList) {
			columnDef.mTableIndex = cursor.getColumnIndex(columnDef.mName);
		}
		mTableIndexSetted = true;
	}

	public int update(OOSqliteEntity entity) {
		if (entity.mDbId == -1) {
			throw new OOSqliteException("Could not update detattch entity!");
		}
		ContentValues values = convertToContentValues(entity);
		String whereClause = entity.idWhereClause();
		return update(values, whereClause);
	}

	public int update(ContentValues values, String whereClause) {
		if (!dbUsable()) {
			return -1;
		}
		return getUsableDb().update(mTableName, values, whereClause, null);
	}

}
