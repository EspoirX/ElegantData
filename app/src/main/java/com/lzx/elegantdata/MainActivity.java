package com.lzx.elegantdata;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lzx.code.ElegantData;
import com.lzx.code.ElegantDataBase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         *   sp文件：
         *   文件名
         *   key-value
         *   存储路径
         *   一般情况下一个项目只有一个sp文件，里面存着key-value，支持多个sp文件
         *
         *   实体类存着key信息，value支持普通类型，支持实体类
         *
         *
         *
         *
         *   file 文件
         *   文件名
         *   存储路径
         *   权限
         *
         *
         *
         */

        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database-name").build();


        LzxDatabase database = new LzxDatabaseImpl();
    }

    @Database(entities = {User.class}, version = 1)
    public static abstract class AppDatabase extends RoomDatabase {


    }

    public static abstract class LzxDatabase extends ElegantDataBase {

        public abstract PreferenceKeyInfo getPreferenceKeyInfo();

    }

    public static class LzxDatabaseImpl extends LzxDatabase {

        @Override
        public PreferenceKeyInfo getPreferenceKeyInfo() {
            return new PreferenceKeyInfo();
        }
    }
}
