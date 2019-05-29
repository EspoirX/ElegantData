package com.lzx.code;

import android.content.Context;

/**
 * create by lzx
 * 2019-05-28
 */
public abstract class ElegantDataBase {

    private static final String DB_IMPL_SUFFIX = "_Impl";

    public static class Builder<T extends ElegantDataBase> {
        private final Class<T> mDatabaseClass;
        private final Context mContext;

        public Builder(Class<T> databaseClass, Context context) {
            mDatabaseClass = databaseClass;
            mContext = context;
        }

        public T build() {
            //noinspection ConstantConditions
            if (mContext == null) {
                throw new IllegalArgumentException("Cannot provide null context for the database.");
            }
            //noinspection ConstantConditions
            if (mDatabaseClass == null) {
                throw new IllegalArgumentException("Must provide an abstract class that"
                        + " extends RoomDatabase");
            }
            T db = ElegantData.getGeneratedImplementation(mDatabaseClass, DB_IMPL_SUFFIX);
            return db;
        }
    }
}
