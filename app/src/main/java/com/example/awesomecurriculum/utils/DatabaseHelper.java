package com.example.awesomecurriculum.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 用户表
        db.execSQL("create table user(" +
                "id integer primary key autoincrement," +
                "username text," +
                "avatar text," +
                "token text," +
                "email text)");
        // 课程数据表
        db.execSQL("create table course(" +
                "id integer primary key," +
                "color text," +
                "courseName text," +
                "teacher text," +
                "classRoom text," +
                "week int," +
                "classStart int," +
                "classEnd int);");
        db.execSQL("create table updateTime(" +
                "id integer primary key," +
                "time text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}