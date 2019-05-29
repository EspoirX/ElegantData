package com.lzx.code;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.annoation.PreferenceEntity;

/**
 * create by lzx
 * 2019-05-28
 */
public class ElegantData {


    public static void inject(Class<?> clazz) {
        PreferenceEntity preferenceEntity = clazz.getAnnotation(PreferenceEntity.class);
        if (preferenceEntity != null) {

        }
    }

    public static <T extends ElegantDataBase> ElegantDataBase.Builder<T> databaseBuilder(
            @NonNull Context context, @NonNull Class<T> klass) {
        return new ElegantDataBase.Builder<>(klass, context);
    }

    static <T, C> T getGeneratedImplementation(Class<C> klass, String suffix) {
        final String fullPackage = klass.getPackage().getName();
        String name = klass.getCanonicalName();
        final String postPackageName = fullPackage.isEmpty()
                ? name
                : (name.substring(fullPackage.length() + 1));
        final String implName = postPackageName.replace('.', '_') + suffix;
        //noinspection TryWithIdenticalCatches
        try {

            @SuppressWarnings("unchecked") final Class<T> aClass = (Class<T>) Class.forName(
                    fullPackage.isEmpty() ? implName : fullPackage + "." + implName);
            return aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("cannot find implementation for "
                    + klass.getCanonicalName() + ". " + implName + " does not exist");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access the constructor"
                    + klass.getCanonicalName());
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to create an instance of "
                    + klass.getCanonicalName());
        }
    }
}
