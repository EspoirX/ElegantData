package com.lzx.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/**
 * 帮助类
 * create by lzx
 * 2019-06-05
 */
class GeneratorHelper {
    static final String IMPL_SUFFIX = "_Impl";
    static final String CODE_PACKAGE_NAME = "com.lzx.code";
    static final String COMPILER_PACKAGE_NAME = "com.lzx.compiler";

    static ClassName getSharedPreferences() {
        return ClassName.get("android.content", "SharedPreferences");
    }

    static String toUpperFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    static boolean isEqualsString(TypeName typeName) {
        return typeName.toString().equals("java.lang.String");
    }
}
