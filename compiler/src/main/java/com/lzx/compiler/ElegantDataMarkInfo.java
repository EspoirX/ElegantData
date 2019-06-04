package com.lzx.compiler;

import com.squareup.javapoet.TypeName;

import java.util.List;

/**
 * create by lzx
 * 2019-06-04
 */
class ElegantDataMarkInfo {
    String markClassName;
    List<FieldInfo> mFieldInfos;

    static class FieldInfo {
        TypeName fieldTypeName;
        String fieldName;
    }
}
