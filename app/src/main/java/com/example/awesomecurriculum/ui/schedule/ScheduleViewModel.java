package com.example.awesomecurriculum.ui.schedule;

import android.widget.Button;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScheduleViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ScheduleViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }

}