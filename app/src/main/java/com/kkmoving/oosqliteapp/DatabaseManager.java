package com.kkmoving.oosqliteapp;

import android.content.Context;

import com.kkmoving.oosqlite.OODatabase;
import com.kkmoving.oosqlite.OOSqliteManager;

public class DatabaseManager {

    public static void init(Context context) {
        OODatabase database = new OODatabase("app.db", 1);
        database.registerEntity(User.class);
        //database.registerEntityWithListener(User.class, User.createListener());

        OOSqliteManager.getInstance().register(database);

        OOSqliteManager.getInstance().initAllDatabase(context);
    }
	
}
