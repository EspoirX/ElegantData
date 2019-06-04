package com.lzx.code;

import android.annotation.SuppressLint;
import android.arch.core.executor.ArchTaskExecutor;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * create by lzx
 * 2019-05-28
 */
public abstract class FileDataBase {

    private static final String DB_IMPL_SUFFIX = "_Impl";
    private SupportFolderCreateHelper mCreateHelper;
    private boolean mAllowMainThreadQueries;

    public void init(@NonNull FileConfiguration configuration) {
        mCreateHelper = createDataFolderHelper(configuration);
        mAllowMainThreadQueries = configuration.allowMainThreadQueries;
    }

    protected abstract SupportFolderCreateHelper createDataFolderHelper(FileConfiguration configuration);

    @NonNull
    public SupportFolderCreateHelper getCreateHelper() {
        return mCreateHelper;
    }

    // used in generated code
    @SuppressLint("RestrictedApi")
    public void assertNotMainThread() {
        if (mAllowMainThreadQueries) {
            return;
        }
        if (ArchTaskExecutor.getInstance().isMainThread()) {
            throw new IllegalStateException("Cannot access database on the main thread since"
                    + " it may potentially lock the UI for a long period of time.");
        }
    }

    public static class Builder<T extends FileDataBase> {
        private final Class<T> mDatabaseClass;
        private final String mDestFileDir;
        private final Context mContext;
        private boolean mAllowMainThreadQueries;
        private SupportFolderCreateHelper.Factory mFactory;

        public Builder(Class<T> databaseClass, String destFileDir, Context context) {
            mDatabaseClass = databaseClass;
            mDestFileDir = destFileDir;
            mContext = context;
        }

        public Builder<T> allowMainThreadQueries() {
            mAllowMainThreadQueries = true;
            return this;
        }

        public T build() {
            if (mFactory == null) {
                mFactory = new DataFolderCreateFactory();
            }
            FileConfiguration configuration =
                    new FileConfiguration(mContext, mDestFileDir, mFactory, mAllowMainThreadQueries);
            T db = ElegantData.getGeneratedImplementation(mDatabaseClass, DB_IMPL_SUFFIX);
            db.init(configuration);
            return db;
        }
    }

}
