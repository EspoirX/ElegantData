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

        User user = new User();
        user.like = "吃屎";

        AppFileDataBase.withSp(this).getKeyInfo().putUser(user);

        User user1 = AppFileDataBase.withFile(this).getKeyInfo().getUser();

        Log.i("xian", "User = " + user1.like);

    }
}
