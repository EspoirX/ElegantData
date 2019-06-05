package com.lzx.elegantdata;

import android.content.Context;

import com.lzx.annoation.ElegantDataMark;
import com.lzx.annoation.PreferenceEntity;
import com.lzx.code.ElegantData;
import com.lzx.code.FileDataBase;

/**
 * create by lzx
 * 2019-06-03
 */
@ElegantDataMark
public abstract class AppFileDataBase extends FileDataBase {

    public abstract PreferenceKeyInfo getPreferenceKeyInfo();

    public abstract KeyInfo getKeyInfo();

    private static AppFileDataBase INSTANCE;
    private static final Object sLock = new Object();

    public static AppFileDataBase withSp(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE =
                        ElegantData.preferenceBuilder(context.getApplicationContext(), AppFileDataBase.class)
                                .build();
            }
            return INSTANCE;
        }
    }

    public static AppFileDataBase withFile(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE =
                        ElegantData.fileBuilder(context.getApplicationContext(), "lzx_file", AppFileDataBase.class)
                                .build();
            }
            return INSTANCE;
        }
    }
}
