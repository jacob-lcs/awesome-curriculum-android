package com.example.awesomecurriculum.data;

import android.util.Log;

import com.example.awesomecurriculum.MainActivity;
import com.example.awesomecurriculum.SettingsActivity;
import com.example.awesomecurriculum.data.model.LoggedInUser;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Response;

/**
 * 该类处理身份验证登录凭据并检索用户信息。
 */
public class LoginDataSource {
    Map map = new HashMap<String, Object>();

    public Result<LoggedInUser> login(final String email, final String password) {
        try {
            // TODO: handle loggedInUser authentication
            Log.d("login", email);
            Log.d("login", password);
            // Android 4.0 之后不能在主线程中请求HTTP请求
            Runnable command = new Runnable() {
                @Override
                public void run() {
                    Log.d("login", "进入子线程");
                    OkHttpUtil.Param[] data = new OkHttpUtil.Param[2];
                    data[0] = new OkHttpUtil.Param("email", email);
                    data[1] = new OkHttpUtil.Param("password", password);
                    Response res;
                    try {
                        res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/user/login", data);

                        Gson gson = new Gson();
                        map = gson.fromJson(res.body().string(), map.getClass());
//                        Log.d("login", (String) Objects.requireNonNull(map.get("username")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            ThreadPoolExecutor res = ThreadPoolManager.getInstance().execute(command);
            res.shutdown();
            while (true) {
                Log.d("login", res.isTerminated() ? "已完成" : "未完成");
                if (res.isTerminated()) {
                    LoggedInUser fakeUser =
                            new LoggedInUser(
                                    java.util.UUID.randomUUID().toString(),
                                    (String) map.get("username"),
                                    (String) map.get("avatar"),
                                    email,
                                    (String) map.get("token"));

                    Log.d("login", "子线程外");

                    return new Result.Success<>(fakeUser);
                }
            }

        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {

    }
}
