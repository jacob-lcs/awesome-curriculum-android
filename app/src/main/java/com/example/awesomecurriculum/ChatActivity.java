package com.example.awesomecurriculum;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.awesomecurriculum.utils.ApplicationUtil;
import com.example.awesomecurriculum.utils.DatabaseHelper;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class ChatActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();
        try {
            initSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(){
        Runnable command = new Runnable() {
            @Override
            public void run() {
                String token = OkHttpUtil.getToken(ChatActivity.this);
                OkHttpUtil.getDataSync("https://coursehelper.online:3000/api/course/queryCourse?token=" + token);
            }
        };
        ThreadPoolExecutor res = ThreadPoolManager.getInstance().execute(command);
        res.shutdown();
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select distinct courseName, courseNo from course where courseNo!=''", null);
        if(cursor.moveToFirst()){
            do {
                LinearLayout container = findViewById(R.id.id_chat_container);
                View v = LayoutInflater.from(this).inflate(R.layout.layout_chat_item, container, false);
                TextView groupName = v.findViewById(R.id.id_group_name);
                String courseName = cursor.getString(cursor.getColumnIndex("courseName"));
                String courseNo = cursor.getString(cursor.getColumnIndex("courseNo"));
                groupName.setText(courseName);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ChatActivity.this, ChatDetailActivity.class);
                        intent.putExtra("courseName", courseName);
                        intent.putExtra("courseNo", courseNo);
                        startActivity(intent);
                    }
                });
                container.addView(v);
            }while (cursor.moveToNext());
        }
    }
    private void initSocket() throws Exception {
        ApplicationUtil appUtil =  (ApplicationUtil) ChatActivity.this.getApplication();
        try {
            appUtil.init();
            Socket socket = appUtil.getSocket();
            socket.on("broadcast message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        Log.d("messages", data.getString("content"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
