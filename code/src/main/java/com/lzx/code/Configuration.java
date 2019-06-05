package com.lzx.code;

import android.content.Context;


/**
 * create by lzx
 * 2019-06-03
 */
public class Configuration {
    public final Context context;
    public final String destFileDir;
    public IFolderCreateHelper.Factory mFactory;

    Configuration(Context context, String destFileDir,
                  IFolderCreateHelper.Factory factory) {
        this.context = context;
        this.destFileDir = destFileDir;
        this.mFactory = factory;
    }
}
