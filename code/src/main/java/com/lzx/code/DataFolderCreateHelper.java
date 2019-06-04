package com.lzx.code;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

/**
 * create by lzx
 * 2019-06-03
 */
public class DataFolderCreateHelper implements SupportFolderCreateHelper {

    private CreateHelper mDelegate;

    DataFolderCreateHelper(Context context, String destFileDir) {
        mDelegate = new CreateHelper(context, destFileDir);
    }

    @Override
    public File getFileDirectory() {
        return mDelegate.getFileDirectory();
    }

    @Override
    public String getFileDirectoryPath() {
        return mDelegate.getFileDirectoryPath();
    }

    @Override
    public Context getContext() {
        return mDelegate.getContext();
    }

    static class CreateHelper {

        private Context context;
        private String destFileDir;
        private File fileDirectory;

        CreateHelper(Context context, String destFileDir) {
            this.context = context;
            this.destFileDir = destFileDir;
            createDestFileDir();
        }

        private void createDestFileDir() {
            if (!TextUtils.isEmpty(destFileDir)) {
                fileDirectory = new File(destFileDir);
                if (!fileDirectory.exists()) {
                    fileDirectory.mkdirs();
                }
            }
            if (fileDirectory == null) {
                fileDirectory = context.getExternalFilesDir(null);
                if (fileDirectory == null) {
                    fileDirectory = context.getFilesDir();
                }
            }
        }

        File getFileDirectory() {
            return fileDirectory;
        }

        String getFileDirectoryPath() {
            return fileDirectory.getAbsolutePath();
        }

        public Context getContext() {
            return context;
        }
    }
}
