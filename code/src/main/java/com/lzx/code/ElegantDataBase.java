package com.lzx.code;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * create by lzx
 * 2019-05-28
 */
public abstract class ElegantDataBase {

    private static final String DB_IMPL_SUFFIX = "_Impl";
    private IFolderCreateHelper mCreateHelper;

    void init(@NonNull Configuration configuration) {
        mCreateHelper = createDataFolderHelper(configuration);
    }

    protected abstract IFolderCreateHelper createDataFolderHelper(Configuration configuration);

    @NonNull
    protected IFolderCreateHelper getCreateHelper() {
        return mCreateHelper;
    }

    public static class Builder<T extends ElegantDataBase> {
        private final Class<T> mDatabaseClass;
        private final String mDestFileDir;
        private final Context mContext;
        private IFolderCreateHelper.Factory mFactory;

        Builder(Class<T> databaseClass, String destFileDir, Context context) {
            mDatabaseClass = databaseClass;
            mDestFileDir = destFileDir;
            mContext = context;
        }

        public T build() {
            if (mFactory == null) {
                mFactory = new FolderCreateFactory();
            }
            Configuration configuration = new Configuration(mContext, mDestFileDir, mFactory);
            T db = ElegantData.getGeneratedImplementation(mDatabaseClass, DB_IMPL_SUFFIX);
            db.init(configuration);
            return db;
        }
    }

}
