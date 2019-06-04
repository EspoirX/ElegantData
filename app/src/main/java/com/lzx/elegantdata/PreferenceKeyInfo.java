package com.lzx.elegantdata;

import android.content.SharedPreferences;

import com.lzx.annoation.PreferenceEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@PreferenceEntity(fileName = "LzxSpFile")
public interface PreferenceKeyInfo {

    String name = "";
    int age = 0;
    float height = 0;
    long width = 0;



}
