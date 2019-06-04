package com.lzx.code;

import android.content.Context;


/**
 * create by lzx
 * 2019-06-03
 */
public class FileConfiguration {
    public final Context context;
    public final String destFileDir;
    public final boolean allowMainThreadQueries;
    public SupportFolderCreateHelper.Factory mFactory;

    FileConfiguration(Context context, String destFileDir,
                      SupportFolderCreateHelper.Factory factory, boolean allowMainThreadQueries) {
        this.context = context;
        this.destFileDir = destFileDir;
        this.mFactory = factory;
        this.allowMainThreadQueries = allowMainThreadQueries;
    }
}
