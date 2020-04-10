package com.example.awesomecurriculum.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jacob
 */
public class ApplicationUtil extends Application {

    public static final String ADDRESS = "https://coursehelper.online";
    public static final int PORT = 5000;

    private Socket mSocket;


    public void init() throws IOException, Exception {
        //与服务器建立连接
        try {
            mSocket = IO.socket(ADDRESS + ":" + PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket.connect();
        Log.d("messages", "正在连接");
        mSocket.on("open", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    Log.d("messages", "连接成功");
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

    public Socket getSocket() {
        return mSocket;
    }

    public void setSocket(Socket socket) {
        this.mSocket = socket;
    }

}
