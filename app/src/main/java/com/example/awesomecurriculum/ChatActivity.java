package com.example.awesomecurriculum;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.awesomecurriculum.utils.DatabaseHelper;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

public class ChatActivity extends AppCompatActivity {
    private Socket mSocket;

    private DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        init();

        try {
            initSocket();
        } catch (JSONException e) {
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
        Cursor cursor = sqLiteDatabase.rawQuery("select distinct courseName from course where courseNo!=''", null);
        if(cursor.moveToFirst()){
            do {
                LinearLayout container = findViewById(R.id.id_chat_container);
                View v = LayoutInflater.from(this).inflate(R.layout.layout_chat_item, container, false);
                TextView groupName = v.findViewById(R.id.id_group_name);
                groupName.setText(cursor.getString(cursor.getColumnIndex("courseName")));
                container.addView(v);
            }while (cursor.moveToNext());
        }
    }

    private void initSocket() throws JSONException {
        try {
            mSocket = IO.socket("https://coursehelper.online:5000");
        } catch (URISyntaxException e) {
            Log.d("error", e.toString());
        }
        mSocket.connect();
        mSocket.on("open", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    openListener();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void openListener() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("from", OkHttpUtil.getToken(this));
        object.put("groups", "");
        object.put("school", OkHttpUtil.getSchool(this));

        mSocket.emit("binding", object);
    }
}
