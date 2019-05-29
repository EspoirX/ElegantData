package com.lzx.elegantdata;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * create by lzx
 * 2019-05-28
 */
@Entity
public class User {
    @PrimaryKey
    public int key;
    public String nickName;
}
