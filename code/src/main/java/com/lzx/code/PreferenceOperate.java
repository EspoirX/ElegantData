package com.lzx.code;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * create by lzx
 * 2019-05-28
 */
public class PreferenceOperate implements IElegantDataOperate {

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public PreferenceOperate(Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreferences() {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        return mSharedPreferences;
    }

    @Override
    public boolean writeDataToFile(String key, String data) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, data);
        return editor.commit();
    }

    @Override
    public String readDataFromFile(String key) {
        return getSharedPreferences().getString(key, "");
    }
}
