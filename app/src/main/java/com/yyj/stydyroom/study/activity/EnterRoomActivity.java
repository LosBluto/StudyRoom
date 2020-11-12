package com.yyj.stydyroom.study.activity;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.http.MyServer;
import com.yyj.stydyroom.base.ui.dialog.DialogMaker;
import com.yyj.stydyroom.base.utils.LogoutHelper;
import com.yyj.stydyroom.base.utils.NetworkUtil;
import com.yyj.stydyroom.base.utils.StringUtil;
import com.yyj.stydyroom.views.data.MyCache;

import java.util.ArrayList;
import java.util.List;

public class EnterRoomActivity extends AppCompatActivity {
    private static final String ISCREATE = "isCreate";
    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final String TAG = "ENTERROOMACTIVITY";

    private TextView roomTip;
    private TextView done;
    private EditText roomEdit;

    private boolean isCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_room);


        isCreate = getIntent().getBooleanExtra(ISCREATE,false);
        parseToolbar();
        parseDone();

        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    public static void start(Context context, boolean isCreate) {
        Intent intent = new Intent();
        intent.putExtra(ISCREATE, isCreate);
        intent.setClass(context, EnterRoomActivity.class);
        context.startActivity(intent);
    }


    /*
    监听器，监听是否登陆
     */
    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode.wontAutoLogin()) {
                LogoutHelper.logout(EnterRoomActivity.this, true);
            }
        }
    };

    /*
    初始化toolbar相关
     */
    private void parseToolbar(){
        roomTip = findViewById(R.id.room_tip);
        roomEdit = findViewById(R.id.room_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (isCreate) {
            roomTip.setText(R.string.room_name);
            toolbar.setTitle(R.string.create_room);
        } else {
            roomTip.setText(R.string.enter_room_id);
            toolbar.setTitle(R.string.search_room);
        }
        setSupportActionBar(toolbar);
    }

    private void parseDone() {
        done = findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetAvailable(EnterRoomActivity.this)) {
                    Toast.makeText(EnterRoomActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(roomEdit.getText().toString().trim())) {
                    Toast.makeText(getApplicationContext(), "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isCreate && !roomEdit.getText().toString().matches("[0-9]+")) {
                    Toast.makeText(getApplicationContext(), "房间号为数字", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (checkPermission()) {
                    Log.i(TAG,"start enter room");
                    createOrEnterRoom();
                } else {
                    Log.i(TAG,"requestPermissions");
                    requestPermissions();
                }
            }
        });
    }

    /*
    权限请求
     */
        private void requestPermissions() {
            final List<String> missed = AVChatManager.checkPermission(this);
            List<String> showRationale = new ArrayList<>();
            for (String permission : missed) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    showRationale.add(permission);
                }
            }

            if (showRationale.size() > 0) {
                new AlertDialog.Builder(this)
                        .setMessage("You need to allow some permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(EnterRoomActivity.this, missed.toArray(new String[missed.size()]),
                                        PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, missed.toArray(new String[missed.size()]), PERMISSION_REQUEST_CODE);
            }
        }

        //检查所有的权限
        private boolean checkPermission() {
            final List<String> missed = AVChatManager.checkPermission(this);
            if (missed.size() == 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case PERMISSION_REQUEST_CODE:
                    Log.i("permission", String.valueOf(AVChatManager.checkPermission(this).size()));
                    if (AVChatManager.checkPermission(this).size() != 0) {
                        createOrEnterRoom();
                    } else {
                        Toast.makeText(EnterRoomActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

        }


    private void createOrEnterRoom() {
//        DialogMaker.showProgressDialog(this, "", false);
        Toast.makeText(this,"",Toast.LENGTH_SHORT).show();
        if (isCreate) {
            Log.i(TAG,"start enterRoom function");
            createRoom();
        } else {
            enterRoom();
        }
    }

    // 创建房间
    private void createRoom() {
        MyServer.getInstance().createRoom(MyCache.getAccount(), roomEdit.getText().toString(), new MyServer.MyCallBack<String>() {
            @Override
            public void onSuccess(String s) {
//                createChannel(s);
                DialogMaker.dismissProgressDialog();
                ChatRoomActivity.start(EnterRoomActivity.this, s, true);
                finish();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
//                DialogMaker.dismissProgressDialog();
                Toast.makeText(EnterRoomActivity.this, "创建房间失败, code:" + code + ", errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 创建会议频道
     */
    private void createChannel(final String roomId) {
        AVChatManager.getInstance().createRoom(roomId, "avchat test", new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
//                DialogMaker.dismissProgressDialog();
                Toast.makeText(getApplicationContext(),"创建成功",Toast.LENGTH_SHORT).show();
                ChatRoomActivity.start(EnterRoomActivity.this, roomId, true);
                finish();
            }

            @Override
            public void onFailed(int i) {
//                DialogMaker.dismissProgressDialog();
                Toast.makeText(EnterRoomActivity.this, "创建频道失败, code:" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable throwable) {
//                DialogMaker.dismissProgressDialog();
            }
        });
    }

    // 进入房间
    private void enterRoom() {
        ChatRoomActivity.start(EnterRoomActivity.this, roomEdit.getText().toString(), false);
        finish();
    }

}