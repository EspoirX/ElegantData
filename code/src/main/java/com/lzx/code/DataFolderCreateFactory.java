package com.lzx.code;

import android.content.Context;

/**
 * create by lzx
 * 2019-06-03
 */
public class DataFolderCreateFactory implements SupportFolderCreateHelper.Factory {
    @Override
    public SupportFolderCreateHelper create(Context context, String name) {
        return new DataFolderCreateHelper(context, name);
    }
}
