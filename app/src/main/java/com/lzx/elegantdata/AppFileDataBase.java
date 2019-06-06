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
public abstract class AppFileDataBase extends ElegantDataBase {

    public abstract PreferenceKeyInfo getPreferenceKeyInfo();

    public abstract KeyInfo getKeyInfo();

    private static AppFileDataBase spInstance;
    private static AppFileDataBase fileInstance;
    private static final Object sLock = new Object();

    public static AppFileDataBase withSp(Context context) {
        synchronized (sLock) {
            if (spInstance == null) {
                spInstance =
                        ElegantData.preferenceBuilder(context.getApplicationContext(), AppFileDataBase.class)
                                .build();
            }
            return spInstance;
        }
    }

    public static AppFileDataBase withFile(Context context) {
        synchronized (sLock) {
            if (fileInstance == null) {
                String path = Environment.getExternalStorageDirectory() + "/aaaaa";
                fileInstance =
                        ElegantData.fileBuilder(context.getApplicationContext(), path, AppFileDataBase.class)
                                .build();
            }
            return fileInstance;
        }
    }
}
