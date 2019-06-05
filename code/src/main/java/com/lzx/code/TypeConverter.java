package com.lzx.code;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create by lzx
 * 2019-06-05
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface TypeConverter {
    Class<? extends JsonParser> value();
}
