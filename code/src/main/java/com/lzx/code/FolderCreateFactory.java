package com.lzx.code;

import android.content.Context;

/**
 * create by lzx
 * 2019-06-03
 */
public class FolderCreateFactory implements IFolderCreateHelper.Factory {
    @Override
    public IFolderCreateHelper create(Context context, String name) {
        return new FolderCreateHelper(context, name);
    }
}
