package com.lzx.code;

/**
 * create by lzx
 * 2019-05-28
 */
public interface IElegantDataOperate {

    boolean writeDataToFile(String key, String data);

    String readDataFromFile(String key);
}
