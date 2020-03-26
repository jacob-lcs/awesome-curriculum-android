package com.example.awesomecurriculum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;


public class ColorPickerFragment extends DialogFragment {
    private Button btn_sure;
    private Button btn_cancel;
    private OnDialogListener mlistener;
    private TextInputEditText colorInput;

    public ColorPickerFragment() {
    }

    public interface OnDialogListener {
        void onDialogClick(String color);
    }
    public void setOnDialogListener(OnDialogListener dialogListener){
        this.mlistener = dialogListener;
    }

    public static ColorPickerFragment newInstance() {
        ColorPickerFragment fragment = new ColorPickerFragment();
        Bundle bundle = new Bundle();
//        bundle.putString("courseName", courseName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_picker, container, false);
        btn_sure = view.findViewById(R.id.btn_save);
        btn_cancel = view.findViewById(R.id.btn_cancel);
        colorInput = view.findViewById(R.id.et_color);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String color = colorInput.getText().toString();
                mlistener.onDialogClick(color);
                dismiss();
            }
        });
    }

}
