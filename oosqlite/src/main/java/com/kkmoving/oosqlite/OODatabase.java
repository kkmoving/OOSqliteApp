/** 
 * Filename:    OODatabase.java
 * Description:  
 * @author:     kkmoving
 * @version:    1.0
 * Create at:   2014-1-14 下午12:44:26
 * 
 * Modification History: 
 * Date         Author      Version     Description 
 * ------------------------------------------------------------------ 
 * 2014-1-14     kkmoving    1.0         1.0 Version
 */
package com.kkmoving.oosqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class OODatabase {

	private String mDbName;

	private List<OOSqliteTable> mTableList;

	private int mVersion;

	OOSqliteOpenHelper mOpenHelper;
	
	private boolean mReady = false;
	
	private OODatabaseListener mListener;

	public OODatabase(String dbName, int version) {
		mDbName = dbName;
		mVersion = version;
		mTableList = new ArrayList<OOSqliteTable>();
	}

    public void registerEntity(Class entityClz) {
        registerEntityWithListener(entityClz, null);
    }

    public void registerEntityWithListener(Class entityClz, OOTableListener listener) {
        registerEntity(entityClz, entityClz.getSimpleName().toLowerCase(), listener);
    }

    public void registerEntityWithTableName(Class entityClz, String tblName) {
        registerEntity(entityClz, tblName, null);
    }

    public void registerEntity(Class entityClz, String tblName, OOTableListener listener) {
        OOSqliteTable table = new OOSqliteTable(entityClz, tblName, listener);
        registerTable(table);
    }

	private void registerTable(OOSqliteTable table) {
        table.init(this);
		mTableList.add(table);
		
		OOSqliteManager.registerTable(table.mCls, table);
	}
	
	public OOSqliteTable getTable(Class cls) {
		return OOSqliteManager.getTable(cls);
	}

	public boolean init(Context context) {
		if (mDbName == null || mDbName.equals("") || mTableList.size() == 0) {
			return false;
		}
		mOpenHelper = new OOSqliteOpenHelper(context, this);
		try {
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			if (db.isOpen()) {
				db.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setReady();
			if (mListener != null) {
				mListener.onDbReady();
			}
		}
		return true;
	}
	
	public void setListener(OODatabaseListener listener) {
		mListener = listener;
	}
	
	synchronized boolean isReady() {
		return mReady;
	}
	
	synchronized void setReady() {
		mReady = true;
	}
	
	String getName() {
		return mDbName;
	}

	int getVersion() {
		return mVersion;
	}

	List<OOSqliteTable> getTableList() {
		return mTableList;
	}
	
	public void recycle() {
		mOpenHelper = null;
	}
}
