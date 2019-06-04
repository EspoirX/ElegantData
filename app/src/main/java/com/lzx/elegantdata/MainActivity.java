package com.lzx.elegantdata;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String name = AppFileDataBase.withSp(this).getPreferenceKeyInfo().getName();
        Toast.makeText(this, "name = " + name, Toast.LENGTH_SHORT).show();
        Log.i("xian", "name = " + name);

    }
}
