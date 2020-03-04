package com.example.awesomecurriculum.ui.schedule;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.awesomecurriculum.R;

public class ScheduleFragment extends Fragment{
    private int maxCoursesNumber = 0;
    private int currentCoursesNumber = 0;
    public static final String AD_URL = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1545749786636&di=fd5483be8b08b2e1f0485e772dadace4&imgtype=0&src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F5f9fae85770bb289f790e08d778516d128f0492a114a8-TNyOSi_fw658";


    private LinearLayout layout;
    private TextView titleTextView;

    private ScheduleViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(ScheduleViewModel.class);
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        createLeftView(13);
    }

    //创建左视图
    private void createLeftView(int n) {
        int endNumber = n;
        if (endNumber > maxCoursesNumber) {
            for (int i = 0; i < endNumber - maxCoursesNumber; i++) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_left_view, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110, 180);
                view.setLayoutParams(params);

                TextView text = view.findViewById(R.id.class_number_text);
                text.setText(String.valueOf(++currentCoursesNumber));

                LinearLayout leftViewLayout = getActivity().findViewById(R.id.left_view_layout);
                leftViewLayout.addView(view);
            }
            maxCoursesNumber = endNumber;
        }
    }
}