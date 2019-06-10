package com.lzx.elegantdata;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * create by lzx
 * 2019-06-10
 */
public class ElegantApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getContext() {
        return sContext;
    }
}
