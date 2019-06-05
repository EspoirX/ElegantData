package com.lzx.elegantdata;

import com.lzx.annoation.EntityClass;
import com.lzx.annoation.IgnoreField;
import com.lzx.annoation.PreferenceEntity;
import com.lzx.code.TypeConverter;

/**
 * create by lzx
 * 2019-05-28
 */
@PreferenceEntity(fileName = "LzxSpFile2")
public interface KeyInfo extends IKeyInfoDao {
    @IgnoreField
    float height = 0;
    long width = 0;
    @TypeConverter(value = SimpleJsonParser.class)
    @EntityClass
    User user = null;
}
