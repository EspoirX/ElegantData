package com.lzx.code;

import android.content.Context;

import java.io.File;

/**
 * create by lzx
 * 2019-06-03
 */
public interface IFolderCreateHelper {

    File getFileDirectory();

    String getFileDirectoryPath();

    Context getContext();

    interface Factory {
        IFolderCreateHelper create(Context context, String destFileDir);
    }
}
