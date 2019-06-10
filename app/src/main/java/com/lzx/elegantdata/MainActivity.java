package com.lzx.elegantdata;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //使用 SP 文件存入数据
        AppDataBase.withSp().getSharedPreferencesInfo().putKeyUserName("小明");
        //使用 File 文件存入数据
        AppDataBase.withFile().getFileCacheInfo().putKeyPassword(123456789);

        String userName = AppDataBase.withSp().getSharedPreferencesInfo().getKeyUserName();
        Log.i("MainActivity", "userName = " + userName);

        int password = AppDataBase.withFile().getFileCacheInfo().getKeyPassword();
        Log.i("MainActivity", "password = " + password);

    }

}
