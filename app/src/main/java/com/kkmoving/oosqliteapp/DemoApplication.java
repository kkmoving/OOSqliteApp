package com.kkmoving.oosqliteapp;

import android.app.Application;

/**
 * Created by kkmoving on 2016/1/4.
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseManager.init(this);
    }
}
