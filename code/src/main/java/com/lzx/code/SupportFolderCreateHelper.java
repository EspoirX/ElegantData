package com.lzx.code;

import android.content.Context;

import java.io.File;

/**
 * create by lzx
 * 2019-06-03
 */
public interface SupportFolderCreateHelper {

    File getFileDirectory();

    String getFileDirectoryPath();

    Context getContext();

    interface Factory {
        SupportFolderCreateHelper create(Context context, String destFileDir);
    }
}
