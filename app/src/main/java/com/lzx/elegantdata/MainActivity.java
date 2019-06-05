package com.lzx.elegantdata;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        User user = new User();
        user.like = "吃屎";

        // AppFileDataBase.withSp(this).getKeyInfo().putUser(user);
        //
        // User user1 = AppFileDataBase.withFile(this).getKeyInfo().getUser();
        //
        // Log.i("xian", "User = " + user1.like);
        writeDataToFile(Environment.getExternalStorageDirectory().getAbsolutePath(), "info.txt", "Hello World");
    }

    private void writeDataToFile(String folder, String fileName, String data) {
        BufferedWriter writer = null;
        FileWriter fileWriter = null;
        try {
            File newFile = new File(folder, fileName);
            fileWriter = new FileWriter(newFile, false);
            writer = new BufferedWriter(fileWriter);
            writer.write(data);
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    fileWriter.close();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String readDataFromFile(String folder, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader;
        FileReader fileReader;
        File file = new File(folder, fileName);
        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
