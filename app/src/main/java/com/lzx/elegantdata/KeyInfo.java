package com.lzx.elegantdata;

import com.lzx.annoation.ElegantEntity;
import com.lzx.code.EntityClass;
import com.lzx.annoation.IgnoreField;

/**
 * create by lzx
 * 2019-05-28
 */
@ElegantEntity(fileName = "LzxFile5.txt", fileType = ElegantEntity.TYPE_FILE)
public interface KeyInfo extends IKeyInfoDao {
    @IgnoreField
    float height = 0;
    long width = 0;

    @EntityClass(value = SimpleJsonParser.class)
    User user = null;
}
