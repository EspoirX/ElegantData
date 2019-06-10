package com.lzx.elegantdata;

import com.lzx.annoation.ElegantEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@ElegantEntity(fileName = "CacheFile.txt", fileType = ElegantEntity.TYPE_FILE)
public interface FileCacheInfo extends IFileCacheInfoDao {
    int keyPassword = 0;
}
