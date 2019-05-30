package com.lzx.elegantdata;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lzx.annoation.PreferenceEntity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    // @Database(entities = {User.class}, version = 1)
    // public static abstract class AppDatabase extends RoomDatabase {
    //
    //
    // }
    //
    // public static abstract class LzxDatabase extends ElegantDataBase {
    //
    //     public abstract PreferenceKeyInfo getPreferenceKeyInfo();
    //
    // }
    //
    // public static class LzxDatabaseImpl extends LzxDatabase {
    //
    //     @Override
    //     public PreferenceKeyInfo getPreferenceKeyInfo() {
    //         return new PreferenceKeyInfo();
    //     }
    // }
}
