package com.kkmoving.oosqlite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.util.List;

class OOSqliteOpenHelper extends SQLiteOpenHelper {

	private static final int WRITE_AHEAD_START_SDK = 16;

	private OODatabase mDatabase;

	public OOSqliteOpenHelper(Context context, OODatabase database) {
		super(context, database.getName(), null, database.getVersion());
		mDatabase = database;

		setDefaultOptions();
	}

	@SuppressLint("NewApi")
	private void setDefaultOptions() {
		if (Build.VERSION.SDK_INT >= WRITE_AHEAD_START_SDK) {
			setWriteAheadLoggingEnabled(true);
		}
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		long in = System.currentTimeMillis();
		List<OOSqliteTable> tableList = mDatabase.getTableList();
		db.beginTransaction();
		try {
			for (OOSqliteTable table : tableList) {
				try {
					table.onCreate(db);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
            e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		long out = System.currentTimeMillis();
        Log.i(Constants.DEBUG_TAG, "create database[" + mDatabase.getName() + "] with time[" + (out - in) + "]");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		long in = System.currentTimeMillis();
		List<OOSqliteTable> tableList = mDatabase.getTableList();
		db.beginTransaction();
		try {
			for (OOSqliteTable table : tableList) {
				try {
					table.onUpgrade(db, oldVersion, newVersion);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
            e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		long out = System.currentTimeMillis();
        Log.i(Constants.DEBUG_TAG, "upgrade database[" + mDatabase.getName() + "] with time[" + (out - in) + "]");
	}

	@Override
	public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		long in = System.currentTimeMillis();
		List<OOSqliteTable> tableList = mDatabase.getTableList();
		db.beginTransaction();
		try {
			for (OOSqliteTable table : tableList) {
				try {
					table.onDowngrade(db, oldVersion, newVersion);
				} catch (Exception e) {
                    e.printStackTrace();
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
            e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		long out = System.currentTimeMillis();
        Log.i(Constants.DEBUG_TAG, "downgrade database[" + mDatabase.getName() + "] with time[" + (out - in) + "]");
	}
	
	public void onOpen(final SQLiteDatabase db) {
		List<OOSqliteTable> tableList = mDatabase.getTableList();
		db.beginTransaction();
		try {
			for (OOSqliteTable table : tableList) {
				try {
					table.onOpen(db);
				} catch (Exception e) {
                    e.printStackTrace();
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
            e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

}
