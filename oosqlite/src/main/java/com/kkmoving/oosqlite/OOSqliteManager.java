/** 
 * Filename:    LeSqliteManager.java
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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OOSqliteManager {

	private List<OODatabase> mDatabaseList;
	
	private static Map<Class, OOSqliteTable> sTableMap = new HashMap<Class, OOSqliteTable>();
	
	private static OOSqliteManager sInstance;
	
	private OOSqliteManager() {
		mDatabaseList = new ArrayList<OODatabase>();
	}
	
	public static OOSqliteManager getInstance() {
		if (sInstance == null) {
			synchronized (OOSqliteManager.class) {
				if (sInstance == null) {
					sInstance = new OOSqliteManager();
				}
			}
		}
		return sInstance;
	}
	
	public static void registerTable(Class cls, OOSqliteTable table) {
		sTableMap.put(cls, table);
	}
	
	public static OOSqliteTable getTable(Class cls) {
		return sTableMap.get(cls);
	}
	
	public void register(OODatabase database) {
		synchronized (mDatabaseList) {
			mDatabaseList.add(database);
		}
	}
	
	public void initAllDatabase(final Context context) {
		if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
			//异步线程直接初始化数据库
			executeInit(context);
		} else {
			//同步线程中，另起线程进行数据库初始化
			new AsyncTask() {
				@Override
				protected Object doInBackground(Object... params) {
					executeInit(context);
					return null;
				}
			} .execute();
		}
	}
	
	private void executeInit(Context context) {
		synchronized (mDatabaseList) {
			for (OODatabase database : mDatabaseList) {
				database.init(context);
			}
		}
	}
	
	public static void recycle() {
		if (sInstance != null) {
			sInstance.release();
		}
	}

    private void release() {
		synchronized (mDatabaseList) {
			for (OODatabase database : mDatabaseList) {
				database.recycle();
			}
        }
    }
}
