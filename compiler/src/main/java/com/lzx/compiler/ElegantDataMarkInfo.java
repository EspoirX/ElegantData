package com.lzx.compiler;

import com.squareup.javapoet.TypeName;

import java.util.List;

/**
 * @ElegantDataMark 信息封装
 * create by lzx
 * 2019-06-04
 */
class ElegantDataMarkInfo {
    String markClassName;
    List<FieldInfo> mFieldInfos;

    static class FieldInfo {
        TypeName fieldTypeName;
        String fieldName;

        String getFieldTypeNameString() {
            String string = fieldTypeName.toString();
            return string.substring(string.lastIndexOf(".") + 1);
        }
    }
}
