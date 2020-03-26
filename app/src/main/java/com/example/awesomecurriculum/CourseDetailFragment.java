package com.example.awesomecurriculum;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;


public class CourseDetailFragment extends DialogFragment {
    public CourseDetailFragment() {
    }

    public static CourseDetailFragment newInstance(String courseName, int week, int start, int end, String teacher, String classRoom, String color, int id, String courseNo) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("courseName", courseName);
        bundle.putInt("week", week);
        bundle.putInt("start", start);
        bundle.putInt("end", end);
        bundle.putInt("id", id);
        bundle.putString("teacher", teacher);
        bundle.putString("classRoom", classRoom);
        bundle.putString("color", color);
        bundle.putString("courseNo", courseNo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);
        editCourse(view.findViewById(R.id.ib_edit));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String courseName = getArguments().getString("courseName");
        String teacher = getArguments().getString("teacher");
        String classRoom = getArguments().getString("classRoom");
        int week = getArguments().getInt("week");
        int start = getArguments().getInt("start");
        int end = getArguments().getInt("end");
        ArrayList<String> weeks = new ArrayList<String>(7);
        weeks.add("周一");
        weeks.add("周二");
        weeks.add("周三");
        weeks.add("周四");
        weeks.add("周五");
        weeks.add("周六");
        weeks.add("周日");
        ((TextView) view.findViewById(R.id.et_course_name)).setText(courseName);
        ((TextView) view.findViewById(R.id.et_time)).setText(weeks.get(week-1) + "  第" + String.valueOf(start) + " - " + String.valueOf(end));
        ((TextView) view.findViewById(R.id.et_teacher)).setText(teacher);
        ((TextView) view.findViewById(R.id.et_room)).setText(classRoom);
        super.onViewCreated(view, savedInstanceState);
    }

    private void editCourse(View edit){
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditCourseActivity.class);
                String courseName = getArguments().getString("courseName");
                String teacher = getArguments().getString("teacher");
                String classRoom = getArguments().getString("classRoom");
                String color = getArguments().getString("color");
                String courseNo = getArguments().getString("courseNo");
                int week = getArguments().getInt("week");
                int start = getArguments().getInt("start");
                int end = getArguments().getInt("end");
                int id = getArguments().getInt("id");
                intent.putExtra("courseName", courseName);
                intent.putExtra("teacher", teacher);
                intent.putExtra("classRoom", classRoom);
                intent.putExtra("week", week);
                intent.putExtra("start", start);
                intent.putExtra("end", end);
                intent.putExtra("color", color);
                intent.putExtra("id", id);
                intent.putExtra("courseNo", courseNo);
                startActivity(intent);
            }
        });
    }
}