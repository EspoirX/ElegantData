package com.lzx.code;

/**
 * create by lzx
 * 2019-05-28
 */
public abstract class PreferenceDao<T> {
    abstract T getValue();

    abstract void setValue(T t);
}
