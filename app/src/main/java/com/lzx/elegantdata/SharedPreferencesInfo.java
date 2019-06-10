package com.lzx.elegantdata;

import com.lzx.annoation.ElegantEntity;

/**
 * create by lzx
 * 2019-05-28
 */
@ElegantEntity(fileName = "UserInfo_Preferences")
public interface SharedPreferencesInfo extends ISharedPreferencesInfoDao {
    String keyUserName = "";
}
