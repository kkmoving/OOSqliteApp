/** 
 * Filename:    LeSqliteEntity.java
 * Description:  
 * @author:     kkmoving
 * @version:    1.0
 * Create at:   2014-1-14 下午5:04:32
 * 
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2014-1-14     kkmoving    1.0         1.0 Version
 */
package com.kkmoving.oosqlite;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public abstract class OOSqliteEntity {
	
	public static final int QUERY_NO_LIMIT = -1;

	private static final String TBL_FIELD_ID = "_id";
	private static final String TBL_FIELD_LAST_ACCESS_TIME = "_last_access";

    public static final OOColumn ID_COLUMN = new OOColumn(TBL_FIELD_ID, OOColumn.ColumnType.LONG, false, true);
	public static final OOColumn LAST_ACCESS_COLUMN = new OOColumn(TBL_FIELD_LAST_ACCESS_TIME, OOColumn.ColumnType.LONG, true);
	
	protected long mDbId = -1;
	protected long mLastAccess;
	
	private static OOSqliteWorker sWorker = new OOSqliteWorker();

	public OOSqliteEntity() {
		
	}

    public static OOColumn getColumn(Class cls, int columnTag) {
        OOSqliteTable table = getTable(cls);
        return table.getColumn(columnTag);
    }

	public boolean attatched() {
		return mDbId != -1;
	}
	
	public static String likeSelection(OOColumn columnDef, Object value, boolean leftLike,
			boolean rightLike) {
		return OOSqliteTable.likeSelection(columnDef, value, leftLike, rightLike);
	}
	
	public static String equalSelection(OOColumn columnDef, Object value) {
		return OOSqliteTable.equalSelection(columnDef, value);
	}
	
	protected static OOSqliteTable getTable(Class cls) {
		return OOSqliteManager.getTable(cls);
	}
	
	protected static boolean insertUnique(OOSqliteEntity entity, OOColumn uniqueCol, Object value) {
		if (uniqueCol != null) {
			OOSqliteTable table = getTable(entity.getClass());
			String selection = table.equalSelection(uniqueCol, value);
			int count = table.countQuery(selection);
			if (count > 0) {
				return false;
			}
		}
		insert(entity);
		return true;
	}
	
	/**
	 * Insert a entity object. Only detatched Entity object could be insert
	 * @param entity detached entity object
	 */
	public static void insert(OOSqliteEntity entity) {
		OOSqliteTable table = getTable(entity.getClass());
		table.insert(entity);
	}
	
	public static void insertAsync(final OOSqliteEntity entity, final LeInsertCallback listener) {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				if (listener == null) {
					insert(entity);
				} else {
					OOSqliteEntity attachedEntity = insertFetch(entity);
					listener.onComplete(attachedEntity);
				}
				
			}
		};
		if (sWorker != null) {
			sWorker.run(runnable);
		}
	}
	
	public static int insertList(List<? extends OOSqliteEntity> list) {
		if (list == null || list.size() <= 0) {
			return -1;
		}
		OOSqliteTable table = getTable(list.get(0).getClass());
		return table.insertList(list);
	}
	
	/**
	 * @param entity detached entity object
	 * @return attached entity object.
	 */
	public static OOSqliteEntity insertFetch(OOSqliteEntity entity) {
		OOSqliteTable table = getTable(entity.getClass());
		return table.insertFetch(entity);
	}
	
	public static List query(Class cls, String selection) {
		return query(cls, selection, QUERY_NO_LIMIT);
	}
	
	public static List query(Class cls, String selection, int limit) {
		return query(cls, selection, null, false, limit);
	}
	
	/**
	 * Query synchronously in order
	 * @param cls target entity class
	 * @param selection sql selection
	 * @param orderCol ordered column
	 * @param asc ascending order
	 * @return list of attached entity object matching selection in order
	 */
	public static List query(Class cls, String selection, OOColumn orderCol, boolean asc) {
		return query(cls, selection, orderCol, asc, QUERY_NO_LIMIT);
	}
	
	public static List query(Class cls, String selection, OOColumn orderCol, boolean asc, int limit) {
		OOSqliteTable table = getTable(cls);
		String sortOrder = null;
		if (orderCol != null) {
			String order = asc ? " ASC" : " DESC ";
			sortOrder = orderCol.mName + order;
		}
		String limitStr = limit == QUERY_NO_LIMIT ? null : String.valueOf(limit);
		return table.query(selection, sortOrder, limitStr);
	}

    public static void queryAsync(final Class cls, final String selection, final LeQueryCallback callback) {
        queryAsync(cls, selection, null, false, QUERY_NO_LIMIT, callback);
    }

    /**
     *
     * Asynchronous query
     *
     * @param cls target entity class
     * @param selection sql selection
     * @param orderCol ordered column
     * @param asc ascending order
     * @param limit max number
     * @param callback callback for query result
     */
	public static void queryAsync(final Class cls, final String selection, final OOColumn orderCol,
			final boolean asc, final int limit, final LeQueryCallback callback) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				List list = query(cls, selection, orderCol, asc, limit);

				if (callback != null) {
					callback.onQuerySuccess(list);
				}
			}
		};
		if (sWorker != null) {
			sWorker.run(runnable);
		}
	}
	
	/**
	 * Only attached entity could be deleted.
	 * @param entity attached entity object
	 * @return number of deleted records
	 */
	public static int delete(OOSqliteEntity entity) {
		OOSqliteTable table = getTable(entity.getClass());
		return table.delete(entity);
	}
	
	public static int delete(Class cls, String whereClause) {
		OOSqliteTable table = getTable(cls);
		return table.delete(whereClause);
	}
	
	/**
	 * Only attached entity could be update.
	 * @param entity attached entity object
	 * @return number of update records
	 */
	public static int update(OOSqliteEntity entity) {
		OOSqliteTable table = getTable(entity.getClass());
		return table.update(entity);
	}
	
	public static void updateAsync(final OOSqliteEntity entity) {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				update(entity);
			}
		};
		if (sWorker != null) {
			sWorker.run(runnable);
		}
	}
	
	static List<OOColumn> buildDbColumns() {
		List<OOColumn> list = new ArrayList<OOColumn>();
		list.add(ID_COLUMN);
		list.add(LAST_ACCESS_COLUMN);
		
		return list;
	}

	static ContentValues convertToContentValues(OOSqliteEntity entity, List<OOColumn> dbColumnList) {
		ContentValues values = new ContentValues();
		for (OOColumn columnDef : dbColumnList) {
			if (columnDef == ID_COLUMN) {
				if (entity.mDbId == -1) {
					//values.put(columnDef.mName, null);
				} else {
					values.put(columnDef.mName, entity.mDbId);
				}
			} else if (columnDef == LAST_ACCESS_COLUMN) {
				entity.mLastAccess = System.currentTimeMillis();
				values.put(columnDef.mName, entity.mLastAccess);
			}
		}
		return values;
	}
	
	static OOSqliteEntity convertFromDb(OOSqliteEntity entity, Map<OOColumn, Object> dbColValueMap) {
		Set<Entry<OOColumn, Object>> colSet = dbColValueMap.entrySet();
		for (Entry<OOColumn, Object> entry : colSet) {
			OOColumn columnDef = entry.getKey();
			Object value = dbColValueMap.get(columnDef);
			if (columnDef == ID_COLUMN) {
				entity.mDbId = (Long) value;
			} else if (columnDef == LAST_ACCESS_COLUMN) {
				entity.mLastAccess = (Long) value;
			}
		}
		return entity;
	}
	
	String idWhereClause() {
		return idWhereClause(mDbId);
	}
	
	static String idWhereClause(long id) {
		return ID_COLUMN.mName + "=" + id;
	}
	
	public interface LeQueryCallback {
		void onQuerySuccess(List list);
	}
	
	public interface LeInsertCallback {
		void onComplete(OOSqliteEntity entity);
	}
}
