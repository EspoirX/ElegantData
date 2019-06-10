package com.lzx.elegantdata;

import android.content.Context;
import android.os.Environment;

import com.lzx.annoation.ElegantDataMark;
import com.lzx.code.ElegantData;
import com.lzx.code.ElegantDataBase;

/**
 * create by lzx
 * 2019-06-03
 */
@ElegantDataMark
public abstract class AppDataBase extends ElegantDataBase {

    public abstract SharedPreferencesInfo getSharedPreferencesInfo();

    public abstract FileCacheInfo getFileCacheInfo();


    private static AppDataBase spInstance;
    private static AppDataBase fileInstance;
    private static final Object sLock = new Object();

    //使用SP文件
    public static AppDataBase withSp() {
        synchronized (sLock) {
            if (spInstance == null) {
                spInstance = ElegantData
                        .preferenceBuilder(ElegantApplication.getContext(), AppDataBase.class)
                        .build();
            }
            return spInstance;
        }
    }

    //使用File文件
    public static AppDataBase withFile() {
        synchronized (sLock) {
            if (fileInstance == null) {
                String path = Environment.getExternalStorageDirectory() + "/ElegantFolder";
                fileInstance = ElegantData
                        .fileBuilder(ElegantApplication.getContext(), path, AppDataBase.class)
                        .build();
            }
            return fileInstance;
        }
    }
}
