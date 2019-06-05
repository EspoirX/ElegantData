package com.lzx.code;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * create by lzx
 * 2019-05-28
 */
public class ElegantData {

    public static <T extends ElegantDataBase> ElegantDataBase.Builder<T> preferenceBuilder(
            @NonNull Context context, @NonNull Class<T> klass) {
        return new ElegantDataBase.Builder<>(klass, "", context);
    }

    public static <T extends ElegantDataBase> ElegantDataBase.Builder<T> fileBuilder(
            @NonNull Context context, String destFileDir, @NonNull Class<T> klass) {
        return new ElegantDataBase.Builder<>(klass, destFileDir, context);
    }


    static <T, C> T getGeneratedImplementation(Class<C> klass, String suffix) {
        final String fullPackage = klass.getPackage().getName();
        String name = klass.getCanonicalName();
        final String postPackageName = fullPackage.isEmpty() ? name : (name.substring(fullPackage.length() + 1));
        final String implName = postPackageName.replace('.', '_') + suffix;
        try {
            @SuppressWarnings("unchecked") final Class<T> aClass = (Class<T>) Class.forName(
                    fullPackage.isEmpty() ? implName : fullPackage + "." + implName);
            return aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
