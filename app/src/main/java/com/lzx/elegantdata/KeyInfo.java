package com.lzx.elegantdata;

import com.lzx.annoation.PreferenceEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@PreferenceEntity(fileName = "LzxSpFile2")
public interface KeyInfo extends IKeyInfoDao {
    float height = 0;
    long width = 0;
}
