package com.example.awesomecurriculum;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectSchoolActivity extends AppCompatActivity {

    private String[] SchoolList = new String[]{"上海大学", "青岛大学"};
    private LinearLayout id_school_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_school);
        init();
    }

    private void init() {
        id_school_container = findViewById(R.id.id_school_container);
        for (String s : SchoolList) {
            View v = LayoutInflater.from(this).inflate(R.layout.layout_chat_item, id_school_container, false);
            TextView groupName = v.findViewById(R.id.id_group_name);
            groupName.setText(s);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SelectSchoolActivity.this, ImportSchoolActivity.class);
                    intent.putExtra("school", s);
                    startActivity(intent);
                }
            });
            id_school_container.addView(v);
        }
    }
}
