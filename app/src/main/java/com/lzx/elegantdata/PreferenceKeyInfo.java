package com.lzx.elegantdata;

import com.lzx.annoation.ElegantEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@ElegantEntity(fileName = "LzxSpFile")
public interface PreferenceKeyInfo extends IPreferenceKeyInfoDao {
    String name = "";
    int age = 0;
}
