package com.lzx.code;

import android.support.annotation.NonNull;

/**
 * json数据解析
 * create by lzx
 * 2019-06-05
 */
public abstract class JsonParser<T> {
    public Class<T> clazz;

    public JsonParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract String convertObject(T object);

    public abstract T onParse(@NonNull String json);
}
