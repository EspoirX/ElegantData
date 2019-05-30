package com.lzx.elegantdata;

import com.lzx.annoation.Embedded;
import com.lzx.annoation.PreferenceEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@PreferenceEntity(fileName = "LzxSpFile")
public abstract class PreferenceKeyInfo {
    public String name;
    public int age;
    @Embedded
    public User user;
}
