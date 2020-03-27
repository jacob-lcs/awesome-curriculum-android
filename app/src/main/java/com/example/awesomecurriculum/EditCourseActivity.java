package com.example.awesomecurriculum;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.awesomecurriculum.ui.login.LoginActivity;
import com.example.awesomecurriculum.utils.DatabaseHelper;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Response;


public class EditCourseActivity extends AppCompatActivity {
    public DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);


    private String courseName;
    private String teacher;
    private String classRoom;
    private String color;
    private String courseNo;
    private int week;
    private int start;
    private int end;
    private int id;

    private AppCompatEditText et_name;
    private AppCompatEditText et_teacher;
    private AppCompatEditText et_class_room;
    private AppCompatTextView tv_color;
    private TextView add_time;

    private Button id_sure;
    private Button id_cancel;

    private List<Object> timeList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);
        changeColor();
        init();
        getData();
        backToMain();
        updateCourse();
        createTimeSelector();
        addTime();
    }

    public static class TimeList {

        public TimeList(int week, int start, int time) {
            this.week = week;
            this.start = start;
            this.time = time;
        }

        int week;
        int start;
        int time;
    }

    private void getData() {
        String[] weeks = new String[7];
        weeks[0] = "周一";
        weeks[1] = "周二";
        weeks[2] = "周三";
        weeks[3] = "周四";
        weeks[4] = "周五";
        weeks[5] = "周六";
        weeks[6] = "周日";
        Intent intent = getIntent();
        courseName = intent.getStringExtra("courseName");
        teacher = intent.getStringExtra("teacher");
        classRoom = intent.getStringExtra("classRoom");
        color = intent.getStringExtra("color");
        courseNo = intent.getStringExtra("courseNo") == null ? "" : intent.getStringExtra("courseNo");

        week = intent.getIntExtra("week", 1);
        id = intent.getIntExtra("id", 1);
        start = intent.getIntExtra("start", 1);
        end = intent.getIntExtra("end", 1);
        et_name.setText(courseName);
        et_teacher.setText(teacher);
        et_class_room.setText(classRoom);
        tv_color.setTextColor(Color.parseColor('#' + color));
    }

    private void init() {
        et_name = findViewById(R.id.et_name);
        et_teacher = findViewById(R.id.et_teacher);
        et_class_room = findViewById(R.id.et_class_room);
        tv_color = findViewById(R.id.tv_color);
        id_sure = findViewById(R.id.id_sure);
        id_cancel = findViewById(R.id.id_cancel);
        id_sure = findViewById(R.id.id_sure);
        add_time = findViewById(R.id.add_time);
    }

    private void addTime(){
        add_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                Gson gson = new Gson();
                String[] weeks = new String[7];
                weeks[0] = "周一";
                weeks[1] = "周二";
                weeks[2] = "周三";
                weeks[3] = "周四";
                weeks[4] = "周五";
                weeks[5] = "周六";
                weeks[6] = "周日";
                final View v = LayoutInflater.from(EditCourseActivity.this).inflate(R.layout.layout_change_time, null);
                TextView t = v.findViewById(R.id.id_time);
                Button btn_delete = v.findViewById(R.id.btn_delete);
                t.setText(weeks[0] + " 第 " + 1 + " - " + 1 + "节");
                LinearLayout container = findViewById(R.id.id_time_container);
                container.addView(v);
                TimeList data2 = new TimeList(1, 1, 1);
                timeList.add(gson.toJson(data2).toString());
                int index = container.indexOfChild(v);
                Log.d("index", String.valueOf(index));
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        final SelectTimeFragment selectTimeDialogFragment = SelectTimeFragment.newInstance(1, 1, 1);
                        selectTimeDialogFragment.show(getSupportFragmentManager(), "time");
                        selectTimeDialogFragment.setOnDialogListener(new SelectTimeFragment.OnDialogListener() {
                            @Override
                            public void onDialogClick(int start, int end, int day) {
                                int index = container.indexOfChild(v);
                                TimeList data = new TimeList(day, start-1, end-start+1);
                                timeList.set(index, gson.toJson(data).toString());
                                t.setText(weeks[day - 1] + " 第 " + start + " - " + end + "节");
                            }
                        });
                    }
                });
                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        int index = container.indexOfChild(v);
                        container.removeView(v);
                        timeList.remove(index);
                    }
                });
            }
        });
    }

    private void backToMain() {
        id_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(EditCourseActivity.this, MainActivity.class);
//                startActivity(intent);
                finish();
            }
        });
    }

    private void updateCourse() {
        final String token = OkHttpUtil.getToken(this);
        Runnable command = new Runnable() {
            @Override
            public void run() {
                Response res;
                try {
                    Map map = new HashMap<String, Object>();
                    SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                    Gson gson = new Gson();
                    String name = et_name.getText().toString();
                    int color = tv_color.getCurrentTextColor();
                    String teacherName = et_teacher.getText().toString();
                    String room = et_class_room.getText().toString();

                    OkHttpUtil.Param[] data1 = new OkHttpUtil.Param[7];
                    data1[0] = new OkHttpUtil.Param("name", name);
                    data1[1] = new OkHttpUtil.Param("teacherName", teacherName);
                    data1[2] = new OkHttpUtil.Param("room", room);
                    data1[3] = new OkHttpUtil.Param("id", String.valueOf(id));
                    Log.d("courseNo", courseNo);
                    data1[4] = new OkHttpUtil.Param("courseNo", courseNo);
                    data1[5] = new OkHttpUtil.Param("timeList", timeList.toString());
                    String c = Integer.toHexString(color).toString();
                    data1[6] = new OkHttpUtil.Param("color", c.substring(2, c.length()));
                    if(id!=0){
                        res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/course/updateCourse?token=" + token, data1);
                    }else{
                        res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/course/addCourse?token=" + token, data1);
                    }

                    map = gson.fromJson(res.body().string(), map.getClass());
                    String codeString = "code";
                    if ((double) map.get(codeString) == 0) {
                        sqLiteDatabase.execSQL("delete from user");
                        Intent intent = new Intent(EditCourseActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d("course", "开始运行");
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        id_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPoolExecutor res = ThreadPoolManager.getInstance().execute(command);
                res.shutdown();
                while (true) {
                    if (res.isTerminated()) {
                        finish();
                        break;
                    }
                }
            }
        });
    }

    private void createTimeSelector() {
        String[] weeks = new String[7];
        weeks[0] = "周一";
        weeks[1] = "周二";
        weeks[2] = "周三";
        weeks[3] = "周四";
        weeks[4] = "周五";
        weeks[5] = "周六";
        weeks[6] = "周日";
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        Gson gson = new Gson();
        Cursor times = sqLiteDatabase.rawQuery("select week, classStart, classEnd from course where courseName='" + courseName + "';", null);
        if(times.moveToFirst()){
            do {
                int week = times.getInt(times.getColumnIndex("week"));
                int classStart = times.getInt(times.getColumnIndex("classStart"));
                int classEnd = times.getInt(times.getColumnIndex("classEnd"));
                TimeList data2 = new TimeList(week, classStart, classEnd);
                timeList.add(gson.toJson(data2).toString());
                final View v = LayoutInflater.from(this).inflate(R.layout.layout_change_time, null);
                TextView t = v.findViewById(R.id.id_time);
                Button btn_delete = v.findViewById(R.id.btn_delete);
                t.setText(weeks[week - 1] + " 第 " + String.valueOf(classStart+1) + " - " + String.valueOf(classStart+classEnd) + "节");
                LinearLayout container = findViewById(R.id.id_time_container);
                container.addView(v);
                int index = container.indexOfChild(v);
                Log.d("index", String.valueOf(index));
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v1) {
                        final SelectTimeFragment selectTimeDialogFragment = SelectTimeFragment.newInstance(1, 1, 1);
                        selectTimeDialogFragment.show(getSupportFragmentManager(), "time");
                        selectTimeDialogFragment.setOnDialogListener(new SelectTimeFragment.OnDialogListener() {
                            @Override
                            public void onDialogClick(int start, int end, int day) {
                                int index = container.indexOfChild(v);
                                TimeList data = new TimeList(day, start-1, end-start+1);
                                timeList.set(index, gson.toJson(data).toString());
                                t.setText(weeks[day - 1] + " 第 " + start + " - " + end + "节");
                            }
                        });
                    }
                });
                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v2) {
                        int index = container.indexOfChild(v);
                        container.removeView(v);
                        timeList.remove(index);
                    }
                });
            }while (times.moveToNext());
        }
    }

    private void changeColor() {
        TextView color = (TextView) findViewById(R.id.iv_color);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("course", "选择颜色");
                final ColorPickerFragment colorPickerDialogFragment = ColorPickerFragment.newInstance();
                colorPickerDialogFragment.show(getSupportFragmentManager(), "color");
                colorPickerDialogFragment.setOnDialogListener(new ColorPickerFragment.OnDialogListener() {
                    @Override
                    public void onDialogClick(String color) {
                        tv_color.setTextColor(Color.parseColor("#" + color));
                    }
                });
            }
        });
    }


}
