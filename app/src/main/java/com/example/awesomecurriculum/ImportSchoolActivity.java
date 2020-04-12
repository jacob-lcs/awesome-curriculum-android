package com.example.awesomecurriculum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.awesomecurriculum.data.model.Course;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;
import com.example.awesomecurriculum.utils.school.SHU;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Response;

public class ImportSchoolActivity extends AppCompatActivity {

    private TextView id_school;
    private EditText id_number;
    private EditText id_password;
    private Button id_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_school);

        init();
    }

    private void init() {
        id_school = findViewById(R.id.id_school);
        id_number = findViewById(R.id.id_number);
        id_password = findViewById(R.id.id_password);
        id_submit = findViewById(R.id.id_submit);
        Intent intent = getIntent();
        String school = intent.getStringExtra("school");
        id_school.setText(school);
        getData();
    }

    private void getData() {
        id_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable command = new Runnable() {
                    @Override
                    public void run() {
                        String id = id_number.getText().toString();
                        String psd = id_password.getText().toString();
                        List<Course> res = SHU.getCourse(id, psd);
                        OkHttpUtil.Param[] params = new OkHttpUtil.Param[1];
                        List courses = new ArrayList<>();
                        for (Course r : res) {
                            Log.d("importCourse", r.toString());
                            JSONObject courseItem = new JSONObject();
                            try {
                                courseItem.put("name", r.getCourseName());
                                courseItem.put("week", r.getWeek());
                                courseItem.put("start", r.getStart());
                                courseItem.put("time", r.getEnd() - r.getStart() + 1);
                                courseItem.put("color", r.getColor());
                                courseItem.put("teacherName", r.getTeacher());
                                courseItem.put("room", r.getClassRoom());
                                courseItem.put("courseNo", r.getCourseNo());
                                courses.add(courseItem);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        params[0] = new OkHttpUtil.Param("courseList", courses.toString());
                        String token = OkHttpUtil.getToken(ImportSchoolActivity.this);
                        try {
                            OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/course/autoImportCourse?token=" + token, params);
                            Intent intent = new Intent(ImportSchoolActivity.this, MainActivity.class);
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                ThreadPoolExecutor response = ThreadPoolManager.getInstance().execute(command);
                response.shutdown();
            }
        });
    }
}
