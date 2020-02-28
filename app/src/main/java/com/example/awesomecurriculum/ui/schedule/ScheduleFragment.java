package com.example.awesomecurriculum.ui.schedule;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.awesomecurriculum.R;
import com.example.awesomecurriculum.data.MySubject;
import com.example.awesomecurriculum.data.SubjectRepertory;
import com.example.awesomecurriculum.ui.login.LoginActivity;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.listener.OnItemBuildAdapter;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.view.WeekView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment implements View.OnClickListener {
    public static final String AD_URL = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1545749786636&di=fd5483be8b08b2e1f0485e772dadace4&imgtype=0&src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F5f9fae85770bb289f790e08d778516d128f0492a114a8-TNyOSi_fw658";

    private TimetableView mTimetableView;
    private WeekView mWeekView;

    private LinearLayout layout;
    private TextView titleTextView;

    private int target = -1;
    private AlertDialog alertDialog;
    private ScheduleViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(ScheduleViewModel.class);
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);
        // final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                // textView.setText(s);
            }
        });
        titleTextView = root.findViewById(R.id.id_title);
//        layout = root.findViewById(R.id.id_layout);
//        layout.setOnClickListener(this);
        initTimetableView(root);

        requestData();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 2秒后刷新界面，模拟网络请求
     */
    private void requestData() {
        alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage("模拟请求网络中..")
                .setTitle("Tips").create();
        alertDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0x123);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (alertDialog != null) {
                alertDialog.hide();
            }
            List<MySubject> mySubjects = SubjectRepertory.loadDefaultSubjects();
            //增加广告
            MySubject adSubject = new MySubject();
            adSubject.setName("【广告】");
            adSubject.setStart(1);
            adSubject.setStep(2);
            adSubject.setDay(7);
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                list.add(i);
            }
            adSubject.setWeekList(list);
            adSubject.setUrl(AD_URL);
            mySubjects.add(adSubject);

            mWeekView.source(mySubjects).showView();
            mTimetableView.source(mySubjects).showView();
        }
    };

    /**
     * 初始化课程控件
     */
    private void initTimetableView(View root) {
        //获取控件
        mWeekView = root.findViewById(R.id.id_weekview);
        mTimetableView = root.findViewById(R.id.id_timetableView);

        //设置周次选择属性
        mWeekView.curWeek(1)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)//设置隐藏，默认显示
                .showView();

        mTimetableView.curWeek(1)
                .curTerm("大三下学期")
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        display(scheduleList);
                    }
                })
                .callback(new ISchedule.OnItemLongClickListener() {
                    @Override
                    public void onLongClick(View v, int day, int start) {
                        Toast.makeText(getActivity(),
                                "长按:周" + day + ",第" + start + "节",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        titleTextView.setText("第" + curWeek + "周");
                    }
                })
                .callback(new OnItemBuildAdapter() {
                    @Override
                    public void onItemUpdate(FrameLayout layout, TextView textView, TextView countTextView, Schedule schedule, GradientDrawable gd) {
                        super.onItemUpdate(layout, textView, countTextView, schedule, gd);
                        if (schedule.getName().equals("【广告】")) {
                            layout.removeAllViews();
                            ImageView imageView = new ImageView(getActivity());
                            imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                            layout.addView(imageView);
                            String url = (String) schedule.getExtras().get(MySubject.EXTRAS_AD_URL);

                            Glide.with(getActivity())
                                    .load(url)
                                    .into(imageView);

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getActivity(), "进入广告网页链接", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .showView();
    }

    /**
     * 更新一下，防止因程序在后台时间过长（超过一天）而导致的日期或高亮不准确问题。
     */
    @Override
    public void onStart() {
        super.onStart();
        mTimetableView.onDateBuildListener()
                .onHighLight();
    }

    /**
     * 周次选择布局的左侧被点击时回调<br/>
     * 对话框修改当前周次
     */
    protected void onWeekLeftLayoutClicked() {
        final String items[] = new String[20];
        int itemCount = mWeekView.itemCount();
        for (int i = 0; i < itemCount; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.curWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mTimetableView.changeWeekForce(target + 1);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 显示内容
     *
     * @param beans
     */
    protected void display(List<Schedule> beans) {
        String str = "";
        for (Schedule bean : beans) {
            str += bean.getName() + "," + bean.getWeekList().toString() + "," + bean.getStart() + "," + bean.getStep() + "\n";
        }
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_layout:
                //如果周次选择已经显示了，那么将它隐藏，更新课程、日期
                //否则，显示
                if (mWeekView.isShowing()) {
                    hideWeekView();
                } else {
                    showWeekView();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 隐藏周次选择，此时需要将课表的日期恢复到本周并将课表切换到当前周
     */
    public void hideWeekView() {
        mWeekView.isShow(false);
        titleTextView.setTextColor(getResources().getColor(R.color.app_course_textcolor_blue));
        int cur = mTimetableView.curWeek();
        mTimetableView.onDateBuildListener()
                .onUpdateDate(cur, cur);
        mTimetableView.changeWeekOnly(cur);
    }

    public void showWeekView() {
        mWeekView.isShow(true);
        titleTextView.setTextColor(getResources().getColor(R.color.app_red));
    }
}