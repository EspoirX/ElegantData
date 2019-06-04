package com.lzx.elegantdata;

import com.lzx.annoation.PreferenceEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@PreferenceEntity(fileName = "LzxSpFile")
public interface PreferenceKeyInfo extends IPreferenceKeyInfoDao {
    String name = "";
    int age = 0;
}
