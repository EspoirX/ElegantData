package com.lzx.elegantdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * create by lzx
 * 2019-05-30
 */
public class PreferenceManager extends PreferenceKeyInfo {
    private final SharedPreferences preference;
    private static PreferenceManager instance;

    private PreferenceManager(Context context) {
        this.preference = context.getSharedPreferences("", Context.MODE_PRIVATE);
    }

    public static PreferenceManager getInstance(@NonNull Context context) {
        if (instance != null) {
            return instance;
        }
        instance = new PreferenceManager(context);
        return instance;
    }

    public boolean putName(String name) {
        return false;
    }


    public boolean putAge(int age) {
        return false;
    }

    private boolean putPreference() {
        SharedPreferences.Editor editor = preference.edit();
        //步骤3：将获取过来的值放入文件
        editor.putString("name", "aa");
        //步骤4：提交
        return editor.commit();
    }
}
