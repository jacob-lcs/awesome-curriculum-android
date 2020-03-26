package com.example.awesomecurriculum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import cn.carbswang.android.numberpickerview.library.NumberPickerView;


public class SelectTimeFragment extends DialogFragment {
    private NumberPickerView wp_day;
    private NumberPickerView wp_start;
    private NumberPickerView wp_end;
    private Button btn_cancel;
    private Button btn_save;

    private int start = 0;
    private int end = 0;
    private int day = 0;
    private OnDialogListener mlistener;

    public SelectTimeFragment() {
    }

    public interface OnDialogListener {
        void onDialogClick(int start, int end, int day);
    }
    public void setOnDialogListener(OnDialogListener dialogListener){
        this.mlistener = dialogListener;
    }

    public static SelectTimeFragment newInstance(int week, int start, int end) {
        SelectTimeFragment fragment = new SelectTimeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("week", week);
        bundle.putInt("start", start);
        bundle.putInt("end", end);
        fragment.setArguments(bundle);
        Log.d("course", "进入时间选择");
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_time, container, false);
        wp_day = view.findViewById(R.id.wp_day);
        wp_start = view.findViewById(R.id.wp_start);
        wp_end = view.findViewById(R.id.wp_end);
        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_save = view.findViewById(R.id.btn_save);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int week = getArguments().getInt("week");
        int _start = getArguments().getInt("start");
        int _end = getArguments().getInt("end");
        day = week;
        start = _start;
        end = _end;
        String[] weeks = new String[7];
        weeks[0] = "周一";
        weeks[1] = "周二";
        weeks[2] = "周三";
        weeks[3] = "周四";
        weeks[4] = "周五";
        weeks[5] = "周六";
        weeks[6] = "周日";
        String[] nodeList = new String[13];
        for (int i = 1; i <= 13; i++) {
            nodeList[i - 1] = "第 " + i + " 节";
        }
        wp_day.setDisplayedValues(weeks);
        wp_start.setDisplayedValues(nodeList);
        wp_end.setDisplayedValues(nodeList);
        initEvent();
//        ((TextView) getView().findViewById(R.id.et_time)).setText(weeks[week-1] + "  第" + String.valueOf(start) + " - " + end);

    }

    private void initEvent() {
        wp_day.setMinValue(0);
        wp_day.setMaxValue(6);
        if (day < 1) {
            day = 1;
        }
        if (day > 7) {
            day = 7;
        }
        wp_day.setValue(day - 1);

        wp_start.setMinValue(0);
        wp_start.setMaxValue(12);
        if (start < 1) {
            start = 1;
        }
        wp_start.setValue(start - 1);

        wp_end.setMinValue(0);
        wp_end.setMaxValue(12);
        if (start < 1) {
            start = 1;
        }
        wp_end.setValue(end - 1);

        wp_day.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView numberPicker, int oldVal, int newVal) {
                day = newVal + 1;

            }
        });

        wp_start.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                start = newVal + 1;
                if (end < start) {
                    wp_end.smoothScrollToValue(start - 1, false);
                    end = start;
                }
            }
        });

        wp_end.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                end = newVal + 1;
                if (end < start) {
                    wp_end.smoothScrollToValue(start - 1, false);
                    end = start;
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mlistener.onDialogClick(start, end, day);
                dismiss();
            }
        });
    }

}