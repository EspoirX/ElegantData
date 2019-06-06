package com.lzx.elegantdata;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.lzx.code.JsonParser;

/**
 * create by lzx
 * 2019-06-05
 */
public class SimpleJsonParser extends JsonParser<User> {

    private Gson mGson;

    public SimpleJsonParser(Class<User> clazz) {
        super(clazz);
        mGson = new Gson();
    }

    @Override
    public String convertObject(User object) {
        return mGson.toJson(object);
    }

    @Override
    public User onParse(@NonNull String json)   {
        return mGson.fromJson(json, User.class);
    }
}
