package com.lzx.elegantdata;

import com.lzx.annoation.ElegantEntity;
import com.lzx.annoation.EntityClass;
import com.lzx.annoation.IgnoreField;
import com.lzx.code.TypeConverter;

import java.io.File;

/**
 * create by lzx
 * 2019-05-28
 */
@ElegantEntity(fileName = "LzxFile", fileType = ElegantEntity.TYPE_FILE)
public interface KeyInfo extends IKeyInfoDao{
    @IgnoreField
    float height = 0;
    long width = 0;
    @TypeConverter(value = SimpleJsonParser.class)
    @EntityClass
    User user = null;
}
