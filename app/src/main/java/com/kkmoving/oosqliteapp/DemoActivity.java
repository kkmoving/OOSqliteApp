package com.kkmoving.oosqliteapp;

import android.app.Activity;
import android.os.Bundle;


public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new DemoView(this));
    }

}
