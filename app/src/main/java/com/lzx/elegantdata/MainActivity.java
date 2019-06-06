package com.lzx.elegantdata;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int index = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppFileDataBase.withFile(MainActivity.this).getKeyInfo().putWidth(index);
                index++;
            }
        });

        findViewById(R.id.query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long width = AppFileDataBase.withFile(MainActivity.this).getKeyInfo().getWidth();
                Log.i("xian", "width = " + width);
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppFileDataBase.withFile(MainActivity.this).getKeyInfo().removeWidth();
            }
        });

        findViewById(R.id.has).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean has = AppFileDataBase.withFile(MainActivity.this).getKeyInfo().containsWidth();
                Toast.makeText(MainActivity.this, "has = " + has, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
