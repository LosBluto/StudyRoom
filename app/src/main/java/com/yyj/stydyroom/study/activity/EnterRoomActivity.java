package com.yyj.stydyroom.study.activity;



import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.lava.nertc.sdk.NERtc;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.http.MyServer;
import com.yyj.stydyroom.base.ui.dialog.DialogMaker;
import com.yyj.stydyroom.base.utils.LogoutHelper;
import com.yyj.stydyroom.base.utils.NetworkUtil;
import com.yyj.stydyroom.study.util.ChatRoomServer;
import com.yyj.stydyroom.study.util.beans.ChatRoom;
import com.yyj.stydyroom.study.util.enums.RoomType;
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
    private Spinner spinner;

    private List<ChatRoom> chatRooms = new ArrayList<>();
    private ChatRoom selectRoom;
    private int roomType = 1;

    ListView chatRoomView;
    BaseAdapter chatRoomAdapter;
    RefreshLayout refreshLayout;

    private boolean isCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_room);

        isCreate = getIntent().getBooleanExtra(ISCREATE,false);
        parseToolbar();
        parseSpinner();
        parseDone();
        getChatRoomViews();
        getChatRoomByRoomType(roomType);
        refresh();


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
            roomEdit.setHint(R.string.enter_room_name);
            toolbar.setTitle(R.string.create_room);
        } else {
            roomTip.setText(R.string.enter_room_id);
            roomEdit.setHint(R.string.enter_room_id);
            toolbar.setTitle(R.string.search_room);
        }
        setSupportActionBar(toolbar);
    }

    private void parseSpinner(){
        spinner = findViewById(R.id.enter_room_listView_spinner);
        List<String> data = new ArrayList<>();

        if (!isCreate)
            data.add("全部");                 //第一个存放查询全部
        for (RoomType roomType:RoomType.values()){
            data.add(roomType.getTypeName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,data);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!isCreate)
                    roomType = position;
                else
                    roomType = position+1;
                getChatRoomByRoomType(roomType);
                Log.d(TAG,"roomType:"+roomType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(!isCreate)
                    roomType = 0;
                else
                    roomType = 1;
                getChatRoomByRoomType(roomType);
                Log.d(TAG,"roomType:"+roomType);
            }
        });
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
            final List<String> missed = NERtc.checkPermission(this);
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
            final List<String> missed = NERtc.checkPermission(this);
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
                    Log.i("permission", String.valueOf(NERtc.checkPermission(this).size()));
                    if (NERtc.checkPermission(this).size() != 0) {
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
            enterRoom(selectRoom==null?roomEdit.getText().toString() : String.valueOf(selectRoom.getId()),roomType);
        }
    }

    // 创建房间
    private void createRoom() {
        MyServer.getInstance().createRoom(roomType,MyCache.getAccount(), roomEdit.getText().toString(), new MyServer.MyCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                DialogMaker.dismissProgressDialog();
                ChatRoomActivity.start(EnterRoomActivity.this, s,roomType, true);
                finish();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
//                DialogMaker.dismissProgressDialog();
                Toast.makeText(EnterRoomActivity.this, "创建房间失败, code:" + code + ", errorMsg:" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 进入房间
    private void enterRoom(String roomId,int roomType) {
        ChatRoomActivity.start(EnterRoomActivity.this, roomId,roomType, false);
        finish();
    }

    private void refresh(){
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(1000/*,false*/);//传入false表示刷新失败
                getChatRoomByRoomType(roomType);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshlayout) {
                refreshlayout.finishLoadMore(1000/*,false*/);//传入false表示加载失败
                getChatRoomByRoomType(roomType);
            }
        });
    }

    private void getChatRoomViews(){
        chatRoomView = findViewById(R.id.enter_room_listView);
        chatRoomAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return chatRooms.size();
            }

            @Override
            public Object getItem(int position) {
                return chatRooms.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                @SuppressLint({"ViewHolder", "InflateParams"})
                View ChatRoomListView = getLayoutInflater().inflate(R.layout.chatroom_listview,null,false);
                TextView room = ChatRoomListView.findViewById(R.id.chat_room_listview_num);
                TextView roomType = ChatRoomListView.findViewById(R.id.chat_room_listview_type);
                Button enter = ChatRoomListView.findViewById(R.id.chat_room_listview_enter);

                String roomId = String.valueOf(chatRooms.get(position).getRoomId());
                room.setText(roomId);
                int roomTypeId = chatRooms.get(position).getRoomType();
                roomType.setText(RoomType.getTypeByTypeId(roomTypeId));

                enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterRoom(roomId,roomTypeId);
                    }
                });

                return ChatRoomListView;
            }
        };
        chatRoomView.setAdapter(chatRoomAdapter);
    }


    private void getChatRooms(){

        ChatRoomServer.getInstance().SelectChatRooms(new ChatRoomServer.ChatRoomCallBack<List<ChatRoom>>() {
            @Override
            public void onSuccess(List<ChatRoom> data) {
//                clearAndInit();
                chatRooms = data;
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(int code, String message) {
                Toast.makeText(getApplicationContext(),"查询失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getChatRoomByRoomType(int id){
        if (id == 0) {          //如果为0则查询全部房间
            getChatRooms();
            return;
        }
        ChatRoomServer.getInstance().selectRoomByRoomType(id, new ChatRoomServer.ChatRoomCallBack<List<ChatRoom>>() {
            @Override
            public void onSuccess(List<ChatRoom> data) {
                chatRooms = data;
                chatRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

    private void getChatRoomByRoomId(int id){
        ChatRoomServer.getInstance().selectChatRoomByRoomId(id, new ChatRoomServer.ChatRoomCallBack<ChatRoom>() {
            @Override
            public void onSuccess(ChatRoom data) {
                selectRoom = data;
            }

            @Override
            public void onFailed(int code, String message) {

            }
        });
    }

    private void clearAndInit(){
        chatRooms = null;
        chatRooms = new ArrayList<>();
    }

}