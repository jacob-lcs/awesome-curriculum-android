package com.example.awesomecurriculum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.awesomecurriculum.adapter.ChatAdapter;
import com.example.awesomecurriculum.bean.AudioMsgBody;
import com.example.awesomecurriculum.bean.FileMsgBody;
import com.example.awesomecurriculum.bean.ImageMsgBody;
import com.example.awesomecurriculum.bean.Message;
import com.example.awesomecurriculum.bean.MsgSendStatus;
import com.example.awesomecurriculum.bean.MsgType;
import com.example.awesomecurriculum.bean.TextMsgBody;
import com.example.awesomecurriculum.utils.ApplicationUtil;
import com.example.awesomecurriculum.utils.ChatUiHelper;
import com.example.awesomecurriculum.utils.DatabaseHelper;
import com.example.awesomecurriculum.utils.FileUtils;
import com.example.awesomecurriculum.utils.LogUtil;
import com.example.awesomecurriculum.utils.OkHttpUtil;
import com.example.awesomecurriculum.utils.PictureFileUtil;
import com.example.awesomecurriculum.utils.ThreadPoolManager;
import com.example.awesomecurriculum.widget.MediaManager;
import com.example.awesomecurriculum.widget.RecordButton;
import com.example.awesomecurriculum.widget.StateButton;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;


public class ChatDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Socket mSocket;
    private String headMessageId;

    private DatabaseHelper databaseHelper = new DatabaseHelper
            (this, "database.db", null, 1);

    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//表情,添加底部布局
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.btn_send)
    StateButton mBtnSend;//发送按钮
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//录音图片
    @BindView(R.id.btnAudio)
    RecordButton mBtnAudio;//录音按钮
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//添加布局
    @BindView(R.id.swipe_chat)
    SwipeRefreshLayout mSwipeRefresh;//下拉刷新
    private ChatAdapter mAdapter;
    public static final String mSenderId = "right";
    public static final String mTargetId = "left";
    public static final int REQUEST_CODE_IMAGE = 0000;
    public static final int REQUEST_CODE_FILE = 2222;

    private String courseName;
    private String courseNo;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        initContent();

        onClicked();
        ApplicationUtil appUtil = (ApplicationUtil) ChatDetailActivity.this.getApplication();
        mSocket = appUtil.getSocket();

        if (ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChatDetailActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        Intent intent = getIntent();
        courseName = intent.getStringExtra("courseName");
        courseNo = intent.getStringExtra("courseNo");
        TextView common_toolbar_title = findViewById(R.id.common_toolbar_title);
        common_toolbar_title.setText(courseName);

        RelativeLayout common_toolbar_back = findViewById(R.id.common_toolbar_back);
        common_toolbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LoadData();

        mSocket.on("broadcast message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    if (data.get("type").toString().equals("text")) {
                        final Message mMessage = getBaseSendMessage(MsgType.TEXT);
                        TextMsgBody mTextMsgBody = new TextMsgBody();
                        try {
                            mTextMsgBody.setMessage(data.getString("content"));
                            mTextMsgBody.setTime(data.getString("time"));
                            mTextMsgBody.setName(data.getJSONObject("from").getString("username"));
                            mTextMsgBody.setAvatar(data.getJSONObject("from").getString("avatar"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mMessage.setBody(mTextMsgBody);
                        OkHttpUtil.Param[] params = new OkHttpUtil.Param[1];
                        Map map = new HashMap<String, Object>();
                        try {
                            params[0] = new OkHttpUtil.Param("id", data.getString("id"));
                            Gson gson = new Gson();
                            String token = OkHttpUtil.getToken(ChatDetailActivity.this);
                            Response res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/message/checkMessageSendByMyself?token=" + token, params);
                            map = gson.fromJson(res.body().string(), map.getClass());
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                        Map finalMap = map;
                        ChatDetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("messages", finalMap.get("res").toString());
                                if (finalMap.get("res").toString() == "true") {
                                    //开始发送
                                    mAdapter.addData(mMessage);
                                    updateMsg(mMessage);
                                } else {
                                    Log.d("messages", "收到了别人发来的消息");
                                    List<Message> mReceiveMsgList = new ArrayList<Message>();
                                    mMessage.setSenderId("收到");
                                    mReceiveMsgList.add(mMessage);
                                    mAdapter.addData(mReceiveMsgList);
                                }
                                mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        });
                    } else if (data.get("type").toString().equals("image")) {
                        final Message mMessage = getBaseSendMessage(MsgType.IMAGE);
                        ImageMsgBody mTextMsgBody = new ImageMsgBody();
                        try {
                            mTextMsgBody.setThumbUrl(data.getString("content"));
                            mTextMsgBody.setTime(data.getString("time"));
                            mTextMsgBody.setName(data.getJSONObject("from").getString("username"));
                            mTextMsgBody.setAvatar(data.getJSONObject("from").getString("avatar"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mMessage.setBody(mTextMsgBody);
                        OkHttpUtil.Param[] params = new OkHttpUtil.Param[1];
                        Map map = new HashMap<String, Object>();
                        try {
                            params[0] = new OkHttpUtil.Param("id", data.getString("id"));
                            Gson gson = new Gson();
                            String token = OkHttpUtil.getToken(ChatDetailActivity.this);
                            Response res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/message/checkMessageSendByMyself?token=" + token, params);
                            map = gson.fromJson(res.body().string(), map.getClass());
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                        Map finalMap = map;
                        ChatDetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("messages", finalMap.get("res").toString());
                                if (finalMap.get("res").toString() == "true") {
                                    //开始发送
                                    mAdapter.addData(mMessage);
                                    updateMsg(mMessage);
                                } else {
                                    Log.d("messages", "收到了别人发来的消息");
                                    List<Message> mReceiveMsgList = new ArrayList<Message>();
                                    mMessage.setSenderId("收到");
                                    mReceiveMsgList.add(mMessage);
                                    mAdapter.addData(mReceiveMsgList);
                                }
                                mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private ImageView ivAudio;

    protected void initContent() {
        ButterKnife.bind(this);
        mAdapter = new ChatAdapter(this, new ArrayList<Message>());
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);
        initChatUi();
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                final boolean isSend = mAdapter.getItem(position).getSenderId().equals(ChatDetailActivity.mSenderId);
                if (ivAudio != null) {
                    if (isSend) {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                    } else {
                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                    }
                    ivAudio = null;
                    MediaManager.reset();
                } else {
                    ivAudio = view.findViewById(R.id.ivAudio);
                    MediaManager.reset();
                    if (isSend) {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
                    } else {
                        ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
                    }
                    AnimationDrawable drawable = (AnimationDrawable) ivAudio.getBackground();
                    drawable.start();
                    MediaManager.playSound(ChatDetailActivity.this, ((AudioMsgBody) mAdapter.getData().get(position).getBody()).getLocalPath(), new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (isSend) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            }

                            MediaManager.release();
                        }
                    });
                }
            }
        });

    }


    @Override
    public void onRefresh() {
        //下拉刷新模拟获取历史消息
        List<Message> mReceiveMsgList = new ArrayList<Message>();
        Runnable command = new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.Param[] params = new OkHttpUtil.Param[1];
                params[0] = new OkHttpUtil.Param("id", headMessageId);
                Gson gson = new Gson();
                String token = OkHttpUtil.getToken(ChatDetailActivity.this);
                Map map = new HashMap<String, Object>();
                ArrayList message = new ArrayList();
                try {
                    Response res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/message/queryHistoryMessage?token=" + token, params);
                    map = gson.fromJson(res.body().string(), map.getClass());
                    message = (ArrayList) map.get("data");
                    if (message.size() != 0) {
                        List<Message> mReceiveMsgList = new ArrayList<Message>();
                        LinkedTreeMap mm = (LinkedTreeMap) message.get(0);
                        headMessageId = mm.get("id").toString();
                        for (int i = message.size() - 1; i >= 0; i--) {
                            LinkedTreeMap m = (LinkedTreeMap) message.get(i);
                            if (m.get("type").toString().equals("text")) {
                                Message mMessage = getBaseSendMessage(MsgType.TEXT);
                                TextMsgBody mTextMsgBody = new TextMsgBody();
                                mTextMsgBody.setMessage(m.get("content").toString());
                                LinkedTreeMap jsonObj = (LinkedTreeMap) m.get("from");
                                mTextMsgBody.setName(jsonObj.get("username").toString());
                                mTextMsgBody.setTime(m.get("time").toString());
                                mTextMsgBody.setAvatar(jsonObj.get("avatar").toString());

                                mMessage.setBody(mTextMsgBody);
                                if (m.get("self").toString().equals("false")) {
                                    mMessage.setSenderId("收到");
                                }
                                mReceiveMsgList.add(mMessage);
                            } else if (m.get("type").toString().equals("image")) {
                                Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
                                ImageMsgBody mImageMsgBody = new ImageMsgBody();
                                mImageMsgBody.setThumbUrl(m.get("content").toString());
                                LinkedTreeMap jsonObj = (LinkedTreeMap) m.get("from");
                                mImageMsgBody.setName(jsonObj.get("username").toString());
                                mImageMsgBody.setTime(m.get("time").toString());
                                mImageMsgBody.setAvatar(jsonObj.get("avatar").toString());
                                mMessgaeImage.setBody(mImageMsgBody);
                                if (m.get("self").toString().equals("false")) {
                                    mMessgaeImage.setSenderId("收到");
                                }
                                mReceiveMsgList.add(mMessgaeImage);
                            }
                        }
                        ChatDetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < mReceiveMsgList.size(); i++) {
                                    if (mReceiveMsgList.get(i).getSenderId() != "收到") {
                                        mAdapter.addData(0, mReceiveMsgList.get(i));
                                        updateMsg(mReceiveMsgList.get(i));
                                    } else {
                                        List<Message> mReceiveMsgLists = new ArrayList<Message>();
                                        mReceiveMsgList.get(i).setSenderId("收到");
                                        mReceiveMsgLists.add(mReceiveMsgList.get(i));
                                        mAdapter.addData(0, mReceiveMsgLists);
                                    }
                                }
                            }
                        });
                    } else {
                        ChatDetailActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatDetailActivity.this, "没有更多消息了哦", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        if (headMessageId != null) {
            ThreadPoolExecutor response = ThreadPoolManager.getInstance().execute(command);
            response.shutdown();
        } else {
            Toast.makeText(ChatDetailActivity.this, "没有更多消息了哦", Toast.LENGTH_SHORT).show();
        }
//        //构建文本消息
//        Message mMessgaeText = getBaseReceiveMessage(MsgType.TEXT);
//        TextMsgBody mTextMsgBody = new TextMsgBody();
//        mTextMsgBody.setMessage("收到的消息");
//        mMessgaeText.setBody(mTextMsgBody);
//        mReceiveMsgList.add(mMessgaeText);
//        //构建图片消息
//        Message mMessgaeImage = getBaseReceiveMessage(MsgType.IMAGE);
//        ImageMsgBody mImageMsgBody = new ImageMsgBody();
//        mImageMsgBody.setThumbUrl("https://c-ssl.duitang.com/uploads/item/201208/30/20120830173930_PBfJE.thumb.700_0.jpeg");
//        mMessgaeImage.setBody(mImageMsgBody);
//        mReceiveMsgList.add(mMessgaeImage);
//        //构建文件消息
//        Message mMessgaeFile = getBaseReceiveMessage(MsgType.FILE);
//        FileMsgBody mFileMsgBody = new FileMsgBody();
//        mFileMsgBody.setDisplayName("收到的文件");
//        mFileMsgBody.setSize(12);
//        mMessgaeFile.setBody(mFileMsgBody);
//        mReceiveMsgList.add(mMessgaeFile);
        mAdapter.addData(0, mReceiveMsgList);
        mSwipeRefresh.setRefreshing(false);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initChatUi() {
        //mBtnAudio
        final ChatUiHelper mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                .bindttToSendButton(mBtnSend)
                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio);
        //底部布局弹出,聊天列表上滑
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });
        //点击空白区域关闭键盘
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                return false;
            }
        });
        //
        ((RecordButton) mBtnAudio).setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                LogUtil.d("录音结束回调");
                File file = new File(audioPath);
                if (file.exists()) {
                    sendAudioMessage(audioPath, time);
                }
            }
        });

    }

    public void onClicked() {
        Button btn_send = findViewById(R.id.btn_send);
        RelativeLayout rlPhoto = findViewById(R.id.rlPhoto);
        RelativeLayout rlFile = findViewById(R.id.rlFile);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendTextMsg(mEtContent.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mEtContent.setText("");
            }
        });
        rlPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureFileUtil.openGalleryPic(ChatDetailActivity.this, REQUEST_CODE_IMAGE);
            }
        });
        rlFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureFileUtil.openFile(ChatDetailActivity.this, REQUEST_CODE_FILE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("获取到的文件路径:" + filePath);
                    sendFileMessage(mSenderId, mTargetId, filePath);
                    break;
                case REQUEST_CODE_IMAGE:
                    // 图片选择结果回调
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        File file = new File(media.getPath());
                        Runnable command = new Runnable() {
                            @Override
                            public void run() {
                                OkHttpUtil.Param[] params = new OkHttpUtil.Param[1];
                                params[0] = new OkHttpUtil.Param("notAvatar", "true");
                                try {
                                    Gson gson = new Gson();
                                    Map map = new HashMap<String, Object>();
                                    Response res = OkHttpUtil.postDataFileSync("https://coursehelper.online:3000/api/file/uploadFile?token=" + OkHttpUtil.getToken(ChatDetailActivity.this), file, "key", params);
                                    map = gson.fromJson(res.body().string(), map.getClass());
                                    Log.d("images", map.toString());
                                    String fileName = (String) map.get("fileName");
                                    JSONObject to = new JSONObject();
                                    to.put("name", courseName);
                                    to.put("courseNo", courseNo);

                                    JSONObject object = new JSONObject();
                                    object.put("from", OkHttpUtil.getToken(ChatDetailActivity.this));
                                    object.put("to", to);
                                    object.put("school", OkHttpUtil.getSchool(ChatDetailActivity.this));
                                    object.put("content", "https://coursehelper.online:3000/" + fileName);
                                    object.put("type", "image");
                                    mSocket.emit("send message", object);
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        ThreadPoolExecutor response = ThreadPoolManager.getInstance().execute(command);
                        response.shutdown();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    //文本消息
    private void sendTextMsg(String msg) throws JSONException {

        JSONObject to = new JSONObject();
        to.put("name", courseName);
        to.put("courseNo", courseNo);

        JSONObject object = new JSONObject();
        object.put("from", OkHttpUtil.getToken(this));
        object.put("to", to);
        object.put("school", OkHttpUtil.getSchool(this));
        object.put("content", msg);
        object.put("type", "text");
        mSocket.emit("send message", object);
    }


    //图片消息
    private void sendImageMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody = new ImageMsgBody();
        mImageMsgBody.setThumbUrl(media.getCompressPath());
        mMessgae.setBody(mImageMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功

        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
        updateMsg(mMessgae);
    }

    //文件消息
    private void sendFileMessage(String from, String to, final String path) {
        final Message mMessgae = getBaseSendMessage(MsgType.FILE);
        FileMsgBody mFileMsgBody = new FileMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
        mFileMsgBody.setSize(FileUtils.getFileLength(path));
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功

        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
        updateMsg(mMessgae);

    }

    //语音消息
    private void sendAudioMessage(final String path, int time) {
        final Message mMessgae = getBaseSendMessage(MsgType.AUDIO);
        AudioMsgBody mFileMsgBody = new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);
        //开始发送
        mAdapter.addData(mMessgae);
        //模拟两秒后发送成功
        updateMsg(mMessgae);
    }


    private Message getBaseSendMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mSenderId);
        mMessgae.setTargetId(mTargetId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private Message getBaseReceiveMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(mTargetId);
        mMessgae.setTargetId(mSenderId);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private void updateMsg(final Message mMessgae) {
        int position = 0;
        mMessgae.setSentStatus(MsgSendStatus.SENT);
        //更新单个子条目
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            Message mAdapterMessage = mAdapter.getData().get(i);
            if (mMessgae.getUuid().equals(mAdapterMessage.getUuid())) {
                position = i;
            }
        }
        mAdapter.notifyItemChanged(position);
    }

    /**
     * @deprecated 获取历史聊天数据
     */
    private void LoadData() {
        OkHttpUtil.Param[] data = new OkHttpUtil.Param[3];
        data[0] = new OkHttpUtil.Param("courseName", courseName);
        data[1] = new OkHttpUtil.Param("courseNo", courseNo);
        data[2] = new OkHttpUtil.Param("school", OkHttpUtil.getSchool(this));
        String token = OkHttpUtil.getToken(this);

        Gson gson = new Gson();
        Runnable command = new Runnable() {
            @Override
            public void run() {

                ArrayList message = new ArrayList();
                Map map = new HashMap<String, Object>();

                try {
                    Response res = OkHttpUtil.postDataSync("https://coursehelper.online:3000/api/message/queryHistoryMessageByCourse?token=" + token, data);
                    map = gson.fromJson(res.body().string(), map.getClass());
                    message = (ArrayList) map.get("data");
                    List<Message> mReceiveMsgList = new ArrayList<Message>();
                    if (message.size() != 0) {
                        LinkedTreeMap mm = (LinkedTreeMap) message.get(message.size() - 1);
                        headMessageId = mm.get("id").toString();
                    }
                    for (int i = message.size() - 1; i >= 0; i--) {
                        LinkedTreeMap m = (LinkedTreeMap) message.get(i);
                        if (m.get("type").toString().equals("text")) {
                            Message mMessage = getBaseSendMessage(MsgType.TEXT);
                            TextMsgBody mTextMsgBody = new TextMsgBody();
                            mTextMsgBody.setMessage(m.get("content").toString());
                            mTextMsgBody.setName(m.get("username").toString());
                            mTextMsgBody.setTime(m.get("time").toString());
                            mTextMsgBody.setAvatar(m.get("avatar").toString());

                            mMessage.setBody(mTextMsgBody);
                            if (m.get("self").toString().equals("false")) {
                                mMessage.setSenderId("收到");
                            }
                            mReceiveMsgList.add(mMessage);
                        } else {
                            Message mMessage = getBaseSendMessage(MsgType.IMAGE);
                            ImageMsgBody mTextMsgBody = new ImageMsgBody();
                            mTextMsgBody.setThumbUrl(m.get("content").toString());
                            mTextMsgBody.setName(m.get("username").toString());
                            mTextMsgBody.setTime(m.get("time").toString());
                            mTextMsgBody.setAvatar(m.get("avatar").toString());

                            mMessage.setBody(mTextMsgBody);
                            if (m.get("self").toString().equals("false")) {
                                mMessage.setSenderId("收到");
                            }
                            mReceiveMsgList.add(mMessage);
                        }

                    }

                    ChatDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < mReceiveMsgList.size(); i++) {
                                if (mReceiveMsgList.get(i).getSenderId() != "收到") {
                                    mAdapter.addData(mReceiveMsgList.get(i));
                                    updateMsg(mReceiveMsgList.get(i));
                                } else {
                                    List<Message> mReceiveMsgLists = new ArrayList<Message>();
                                    mReceiveMsgLists.add(mReceiveMsgList.get(i));
                                    mAdapter.addData(mReceiveMsgLists);
                                }
                            }
                            mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        ThreadPoolExecutor response = ThreadPoolManager.getInstance().execute(command);
        response.shutdown();
    }


}
