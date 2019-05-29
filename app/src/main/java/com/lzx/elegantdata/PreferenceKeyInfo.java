package com.lzx.elegantdata;

import com.lzx.annoation.PreferenceEntity;
import com.lzx.code.IElegantDataOperate;

/**
 * create by lzx
 * 2019-05-28
 */
@PreferenceEntity(fileName = "sp_file")
public class PreferenceKeyInfo {
    public String name;
    public String age;

    private IElegantDataOperate mOperate;


    public PreferenceKeyInfo(IElegantDataOperate operate) {
        mOperate = operate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
