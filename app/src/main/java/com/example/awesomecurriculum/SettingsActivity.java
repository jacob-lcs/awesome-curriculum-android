package com.example.awesomecurriculum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.awesomecurriculum.ui.login.LoginActivity;
import com.example.awesomecurriculum.utils.DatabaseHelper;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Response;

public class SettingsActivity extends AppCompatActivity {

    private Button idQuitLogin;
    private ImageView idAvatar;
    private TextView idNickName;
    private TextView idEmail;

    private DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init(){
        idQuitLogin = findViewById(R.id.id_quit_button);
        idAvatar = findViewById(R.id.id_avatar);
        idNickName = findViewById(R.id.id_nick_name);
        idEmail = findViewById(R.id.id_email);

        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from user;", null);
        cursor.moveToFirst();
        idEmail.setText(cursor.getString(cursor.getColumnIndex("email")));
        idNickName.setText(cursor.getString(cursor.getColumnIndex("username")));
        final Bitmap[] bitmap = new Bitmap[1];
        Runnable command = new Runnable() {
            @Override
            public void run() {
                bitmap[0] =
                        getHttpBitmap("https://coursehelper.online:3000/" + cursor.getString(cursor.getColumnIndex("avatar")));
            }
        };
        ThreadPoolExecutor res = ThreadPoolManager.getInstance().execute(command);
        res.shutdown();

        while (true){
            if(res.isTerminated()){
                idAvatar.setImageBitmap(bitmap[0]);
                break;
            }
        }

        idQuitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitLogin();
            }
        });
    }

    private void quitLogin(){
        Runnable command = new Runnable() {
            @Override
            public void run() {
                String token = OkHttpUtil.getToken(SettingsActivity.this);
                OkHttpUtil.getDataSync("https://coursehelper.online:3000/api/course/queryCourse?token=" + token);
            }
        };
        ThreadPoolExecutor res = ThreadPoolManager.getInstance().execute(command);
        res.shutdown();
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from user");
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public static Bitmap getHttpBitmap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
