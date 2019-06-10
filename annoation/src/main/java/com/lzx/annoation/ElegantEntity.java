package com.lzx.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Stack;

/**
 * create by lzx
 * 2019-05-28
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ElegantEntity {

    int TYPE_PREFERENCE = 0;
    int TYPE_FILE = 1;

    String fileName() default "";

    int fileType() default TYPE_PREFERENCE;
}
