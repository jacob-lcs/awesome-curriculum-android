package com.example.awesomecurriculum;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.example.awesomecurriculum.ui.login.LoginActivity;
import com.example.awesomecurriculum.utils.DatabaseHelper;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Jacob
 */
public class MainActivity extends AppCompatActivity {

    public DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!logined()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * @return boolean
     * @deprecated 判断用户是否登录
     */
    private boolean logined() {
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from user;", null);
        Log.d("login", String.valueOf(cursor.getCount()));
        boolean result = cursor.getCount() != 0;
        cursor.close();
        return result;
    }

}
