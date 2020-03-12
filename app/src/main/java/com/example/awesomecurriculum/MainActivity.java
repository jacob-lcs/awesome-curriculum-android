package com.example.awesomecurriculum;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awesomecurriculum.data.model.Course;
import com.example.awesomecurriculum.ui.login.LoginActivity;
import com.example.awesomecurriculum.utils.DatabaseHelper;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Response;

/**
 * @author Jacob
 */
public class MainActivity extends AppCompatActivity {
    Map map = new HashMap<String, Object>();
    Map updateTimeMap = new HashMap<String, Object>();
    /**
     * 星期几
     */
    private RelativeLayout day;

    private int currentCoursesNumber = 0;

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
        createLeftView();
        loadData();
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

    /**
     * 创建左视图
     */
    private void createLeftView() {
        int endNumber = 13;
        for (int i = 0; i < endNumber; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_left_view, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110, 180);
            view.setLayoutParams(params);

            TextView text = view.findViewById(R.id.class_number_text);
            text.setText(String.valueOf(++currentCoursesNumber));

            LinearLayout leftViewLayout = findViewById(R.id.left_view_layout);
            leftViewLayout.addView(view);
        }
    }

    /**
     * 获取课表数据
     */
    private void loadData() {
        ArrayList<Course> coursesList = new ArrayList<>();
        final String token = OkHttpUtil.getToken(this);
        Runnable command = new Runnable() {
            @Override
            public void run() {
                Log.d("login", "进入子线程");
                OkHttpUtil.Param[] data = new OkHttpUtil.Param[2];
                Response res;
                try {
                    SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                    Cursor lastTime = sqLiteDatabase.rawQuery("select * from updateTime", null);
                    Response updateTime = OkHttpUtil.getDataSync("https://coursehelper.online:3000/api/course/queryUpdateTime?token=" + token);
                    Gson gson = new Gson();
                    updateTimeMap = gson.fromJson(updateTime.body().string(), updateTimeMap.getClass());
                    Log.d("course", String.valueOf(updateTimeMap.get("time")));
                    lastTime.moveToFirst();
                    String last = "0";
                    if (lastTime.getCount() != 0) {
                        last = lastTime.getString(lastTime.getColumnIndex("time"));
                    }else{
                        sqLiteDatabase.execSQL("insert into updateTime (id, time) values (1, "+String.valueOf(updateTimeMap.get("time"))+")");
                    }
                    Log.d("course", last);
                    if (last.compareTo(String.valueOf(updateTimeMap.get("time"))) < 0) {
                        res = OkHttpUtil.getDataSync("https://coursehelper.online:3000/api/course/queryCourse?token=" + token);
                        map = gson.fromJson(res.body().string(), map.getClass());
                        Log.d("course", "获取成功");
                        String codeString = "code";
                        if ((double) map.get(codeString) == 0) {
                            sqLiteDatabase.execSQL("delete from user");
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            sqLiteDatabase.execSQL("delete from course");
                            Log.d("course", "开始运行");
                            ArrayList<Object> response = (ArrayList<Object>) map.get("data");
                            for(int i=0; i<response.size(); i++){
                                JsonObject returnData = new JsonParser().parse(response.get(i).toString()).getAsJsonObject();
                                Log.d("course", String.valueOf(returnData.get("name")));
                                sqLiteDatabase.execSQL("insert into course (id, color, courseName, teacher, classRoom, week, classStart, classEnd) values ("+
                                        returnData.get("id")+","+returnData.get("color")+","+returnData.get("name")
                                        +","+returnData.get("teacherName")
                                        +","+returnData.get("room")
                                        +","+returnData.get("week")
                                        +","+returnData.get("start")
                                        +","+returnData.get("time")+");");
                            }

                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        ThreadPoolExecutor res = ThreadPoolManager.getInstance().execute(command);
        res.shutdown();
        while (true) {
            if (res.isTerminated()) {
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                Cursor courses = sqLiteDatabase.rawQuery("select * from course", null);
                if (courses.moveToFirst()) {
                    do {
                        coursesList.add(new Course(
                                courses.getString(courses.getColumnIndex("courseName")),
                                courses.getString(courses.getColumnIndex("teacher")),
                                courses.getString(courses.getColumnIndex("classRoom")),
                                courses.getInt(courses.getColumnIndex("week")),
                                courses.getInt(courses.getColumnIndex("classStart"))+1,
                                courses.getInt(courses.getColumnIndex("classStart"))+courses.getInt(courses.getColumnIndex("classEnd")),
                                courses.getString(courses.getColumnIndex("color")),
                                courses.getInt(courses.getColumnIndex("id"))));
                    } while (courses.moveToNext());
                }
                courses.close();
                for(int i=0; i<coursesList.size(); i++){
                    createItemCourseView(coursesList.get(i));
                }
                break;
            }
        }
    }
    /**
     * 创建单个课程视图
     */
    private void createItemCourseView(final Course course) {
        final android.content.Context that = this;
        int getDay = course.getWeek();
        if ((getDay < 1 || getDay > 7) || course.getStart() > course.getEnd()) {
            Toast.makeText(this, "星期几没写对,或课程结束时间比开始时间还早~~", Toast.LENGTH_LONG).show();
        } else {
            int dayId = 0;
            switch (getDay) {
                case 1:
                    dayId = R.id.monday;
                    break;
                case 2:
                    dayId = R.id.tuesday;
                    break;
                case 3:
                    dayId = R.id.wednesday;
                    break;
                case 4:
                    dayId = R.id.thursday;
                    break;
                case 5:
                    dayId = R.id.friday;
                    break;
                case 6:
                    dayId = R.id.saturday;
                    break;
                case 7:
                    dayId = R.id.weekday;
                    break;
                default:
                    break;
            }
            day = findViewById(dayId);

            int height = 180;
            final View v = LayoutInflater.from(this).inflate(R.layout.activity_course_card, null);
            //加载单个课程布局
            v.setY(height * (course.getStart() - 1));
            //设置开始高度,即第几节课开始
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, (course.getEnd() - course.getStart() + 1) * height - 8);
            //设置布局高度,即跨多少节课
            v.setLayoutParams(params);
            v.setBackgroundColor(Color.parseColor("#"+course.getColor()));
            TextView text = v.findViewById(R.id.text_view);
            text.setText(course.getCourseName() + "\n" + course.getTeacher() + "\n" + course.getClassRoom());
            //显示课程名
//            String [] a = {"#f17c67","#9966CC","#BDB76A","#008573","#FE4C40","#DE3163"};
//            Random random = new Random();
//            int n = random.nextInt(6);
//            v.findViewById(R.id.CardView1).setBackgroundColor(Color.parseColor(a[n]));
            v.findViewById(R.id.CardView1).getBackground().setAlpha(200);
            day.addView(v);

        }
    }
}
