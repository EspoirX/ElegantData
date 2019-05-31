package com.lzx.elegantdata;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LzxSpFileImpl.getInstance(this).putName("dasldkhasldk");

        String name = LzxSpFileImpl.getInstance(this).getNameSync();
        Log.i("XIAN", "name = " + name);



    }
}
