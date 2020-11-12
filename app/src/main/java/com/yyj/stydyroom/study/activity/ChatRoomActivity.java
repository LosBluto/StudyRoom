package com.yyj.stydyroom.study.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.ui.TActivity;
import com.yyj.stydyroom.base.ui.dialog.DialogMaker;
import com.yyj.stydyroom.study.fragment.ChatRoomRTSFragment2;
import com.yyj.stydyroom.study.fragment.ChatRoomRootFragment;
import com.yyj.stydyroom.study.fragment.OnlinePeopleFragment;
import com.yyj.stydyroom.study.helper.ChatRoomMemberCache;
import com.yyj.stydyroom.study.helper.MsgHelper;
import com.yyj.stydyroom.study.helper.VideoListener;
import com.yyj.stydyroom.study.util.ShareType;
import com.yyj.stydyroom.views.data.MyCache;

import java.util.Map;

public class ChatRoomActivity extends TActivity implements VideoListener {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    private final static String EXTRA_ROOM_ID = "ROOM_ID";

    private final static String EXTRA_MODE = "EXTRA_MODE";

    private final static String EXTRA_ROOM_INFO = "EXTRA_ROOM_INFO";


    /**
     * 聊天室基本信息
     */
    private String roomId;

    private ChatRoomInfo roomInfo;

    private boolean isCreate; // true 老师模式，false 观众模式

    private String sessionId; // 多人白板sessionId

    private String sessionName;

    private ChatRoomRootFragment rootFragment;

    private ChatRoomRTSFragment2 rtsFragment;

    private OnlinePeopleFragment onlinePeopleFragment;

    private AbortableFuture<EnterChatRoomResultData> enterRequest;

    private boolean isFirstComing = true; // 主播是否首次进入房间

    private AVChatData data;

    Context context;


    public static void start(Context context, String roomId, boolean isCreate) {
        Intent intent = new Intent();
        intent.setClass(context, ChatRoomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_MODE, isCreate);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        parseIntent(savedInstanceState);
        // 注册监听
        registerObservers(true);
        // 登录聊天室
        enterRoom();
        context = this;
    }

    @Override
    protected void onDestroy() {
        registerObservers(false);
        if (!TextUtils.isEmpty(sessionName)) {
            Log.i(TAG, "unregister rts observers");
            registerRTSObservers(sessionName, false);
        }
        if (rootFragment != null) {
            rootFragment.onKickOut();
            rootFragment = null;
        }
        super.onDestroy();
    }

    private void parseIntent(Bundle savedInstanceState) {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        isCreate = getIntent().getBooleanExtra(EXTRA_MODE, false);
        if (savedInstanceState != null) {
            roomInfo = (ChatRoomInfo) savedInstanceState.getSerializable(EXTRA_ROOM_INFO);
        }
    }

    @Override
    public void onBackPressed() {
        if (rootFragment != null) {
            rootFragment.onBackPressed();
        }
    }

    private void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeOnlineStatus(onlineStatus, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeKickOutEvent(kickOutObserver, register);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode.wontAutoLogin()) {
                NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
                if (rootFragment != null) {
                    rootFragment.onKickOut();
                }
            }
        }
    };

    Observer<ChatRoomStatusChangeData> onlineStatus = new Observer<ChatRoomStatusChangeData>() {

        @Override
        public void onEvent(ChatRoomStatusChangeData chatRoomStatusChangeData) {
            if (chatRoomStatusChangeData.status == StatusCode.CONNECTING) {
                DialogMaker.updateLoadingMessage("连接中...");
            } else if (chatRoomStatusChangeData.status == StatusCode.UNLOGIN) {
                if (NIMClient.getService(ChatRoomService.class).getEnterErrorCode(roomId) ==
                        ResponseCode.RES_CHATROOM_STATUS_EXCEPTION) {
                    // 聊天室连接状态异常
                    Toast.makeText(ChatRoomActivity.this, R.string.chatroom_status_exception, Toast.LENGTH_SHORT)
                            .show();
                    NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
                    if (rootFragment != null) {
                        rootFragment.onKickOut();
                    }
                } else {
                    Toast.makeText(context, R.string.nim_status_unlogin, Toast.LENGTH_SHORT).show();
                    onOnlineStatusChanged(false);
                }
            } else if (chatRoomStatusChangeData.status == StatusCode.LOGINING) {
                DialogMaker.updateLoadingMessage("登录中...");
            } else if (chatRoomStatusChangeData.status == StatusCode.LOGINED) {
                onOnlineStatusChanged(true);
            } else if (chatRoomStatusChangeData.status == StatusCode.NET_BROKEN) {
                Toast.makeText(context, R.string.net_broken, Toast.LENGTH_SHORT).show();
                onOnlineStatusChanged(false);
            }
            Log.i(TAG, "Chat Room Online Status:" + chatRoomStatusChangeData.status.name());
        }
    };

    Observer<ChatRoomKickOutEvent> kickOutObserver = new Observer<ChatRoomKickOutEvent>() {

        @Override
        public void onEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
            if (chatRoomKickOutEvent.getReason() == ChatRoomKickOutEvent.ChatRoomKickOutReason.CHAT_ROOM_INVALID) {
                if (!roomInfo.getCreator().equals(MyCache.getAccount())) {
                    Toast.makeText(context, R.string.meeting_closed, Toast.LENGTH_SHORT).show();
                }
            } else if (chatRoomKickOutEvent.getReason() ==
                    ChatRoomKickOutEvent.ChatRoomKickOutReason.KICK_OUT_BY_MANAGER) {
                Toast.makeText(context, R.string.kick_out_by_master, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "被踢出聊天室，reason:" + chatRoomKickOutEvent.getReason(), Toast.LENGTH_SHORT).show();
            }
            if (rootFragment != null) {
                rootFragment.onKickOut();
            }
        }
    };

    private void enterRoom() {
        DialogMaker.showProgressDialog(this, null, "", true, dialog -> {
            if (enterRequest != null) {
                enterRequest.abort();
                onLoginDone();
                finish();
            }
        }).setCanceledOnTouchOutside(false);
        EnterChatRoomData data = new EnterChatRoomData(roomId);
        enterRequest = NIMClient.getService(ChatRoomService.class).enterChatRoom(data);
        enterRequest.setCallback(new RequestCallback<EnterChatRoomResultData>() {

            @Override
            public void onSuccess(EnterChatRoomResultData result) {
                if (isFinishing()) {
                    return;
                }
                roomInfo = result.getRoomInfo();
                if (roomInfo == null) {
                    enterRoomFailed("该聊天室不存在");
                    return;
                }
                if (isCreate) {
                    updateRoomInfo(roomId, result);
                    return;
                }
                enterRoomSuccess(result);
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_CHATROOM_BLACKLIST) {
                    enterRoomFailed("你已被拉入黑名单，不能再进入");
                } else if (code == ResponseCode.RES_ENONEXIST) {
                    enterRoomFailed("该聊天室不存在");
                } else {
                    enterRoomFailed("enter chat room failed, code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {
                enterRoomFailed("enter chat room exception, e=" + exception.getMessage());
            }
        });
    }


    private void updateRoomInfo(final String roomId, final EnterChatRoomResultData roomResultData) {
        MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, null, roomId, new RequestCallback<Void>() {

            @Override
            public void onSuccess(Void param) {
                enterRoomSuccess(roomResultData);
            }

            @Override
            public void onFailed(int code) {
                enterRoomFailed("更新房间信息失败, code:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                enterRoomFailed("更新房间信息失败, exception:" + exception);
            }

        });
    }

    private void enterRoomFailed(String toastText) {
        onLoginDone();
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void enterRoomSuccess(EnterChatRoomResultData roomResultData) {
        onLoginDone();
        sessionId = roomInfo.getRoomId();
        ChatRoomMember member = roomResultData.getMember();
        member.setRoomId(roomInfo.getRoomId());
        ChatRoomMemberCache.getInstance().saveMyMember(member);
        initRootFragment();
        registerRTSObservers(roomInfo.getRoomId(), true);
    }


    private void onLoginDone() {
        enterRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    private void initRootFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment oldFragment = fragmentManager.findFragmentByTag("ChatRoomRootFragment");
        if (oldFragment != null) {
            return;
        }
        rootFragment = ChatRoomRootFragment.newInstance(roomInfo, isCreate);
        fragmentManager.beginTransaction().add(R.id.chat_room_fragment_container, rootFragment, "ChatRoomRootFragment")
                .commit();
    }

    private void registerRTSObservers(String sessionName, boolean register) {
        this.sessionName = sessionName;
    }

    private void onOnlineStatusChanged(final boolean isOnline) {
        if (!isOnline) {
            if (rootFragment != null) {
                rootFragment.onOnlineStatusChanged(false, roomInfo);
            }
            return;
        }
        NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomId).setCallback(
                new RequestCallback<ChatRoomInfo>() {

                    @Override
                    public void onSuccess(ChatRoomInfo chatRoomInfo) {
                        roomInfo = chatRoomInfo;
                        if (rootFragment != null) {
                            rootFragment.onOnlineStatusChanged(true, roomInfo);
                        }
                    }

                    @Override
                    public void onFailed(int i) {
                    }

                    @Override
                    public void onException(Throwable throwable) {
                    }
                });
    }

    private void ensureChildFragment() {
        if (onlinePeopleFragment == null) {
            onlinePeopleFragment = (OnlinePeopleFragment) getSupportFragmentManager().findFragmentById(
                    R.id.online_people_fragment);
        }
        if (rtsFragment == null) {
            rtsFragment = (ChatRoomRTSFragment2) getSupportFragmentManager().findFragmentById(
                    R.id.chat_room_rts_fragment);
            rtsFragment.onAVChatData(data);
        }
    }

    public ChatRoomInfo getRoomInfo() {
        return roomInfo;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (roomInfo != null) {
            outState.putSerializable(EXTRA_ROOM_INFO, roomInfo);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ensureChildFragment();
        if (rtsFragment != null) {
            rtsFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAVChatData(AVChatData data) {
        if (rtsFragment != null) {
            rtsFragment.onAVChatData(data);
        } else {
            // hold
            this.data = data;
        }
    }

    @Override
    public void onVideoOn(String account) {
        if (rootFragment != null) {
            rootFragment.studentPermissionOn(account);
        }
    }

    @Override
    public void onVideoOff(String account) {
        if (rootFragment != null) {
            rootFragment.closeStudentPermission(account);
        }
    }

    @Override
    public void onTabChange(boolean notify) {
        if (rootFragment != null) {
            rootFragment.onTabChange(notify);
        }
    }

    @Override
    public void onKickOutSuccess(String account) {
        if (rootFragment != null) {
            rootFragment.onKickOutSuccess(account);
        }
    }


    @Override
    public void onReportSpeaker(Map<String, Integer> map) {
        ensureChildFragment();
        if (onlinePeopleFragment != null) {
            onlinePeopleFragment.onReportSpeaker(map);
        }
    }

    @Override
    public void onAcceptConfirm() {
        ensureChildFragment();
        if (onlinePeopleFragment != null) {
            onlinePeopleFragment.onAcceptConfirm();
        }
        if (rtsFragment != null) {
            rtsFragment.onAcceptConfirm();
        }
    }

    @Override
    public void onReject() {
        ensureChildFragment();
        if (rtsFragment != null) {
            rtsFragment.onReject();
        }
    }

    @Override
    public void onLineFragNotify() {
        ensureChildFragment();
        if (onlinePeopleFragment != null) {
            onlinePeopleFragment.onLineFragNotify();
        }
    }

    @Override
    public void onStatusNotify() {
        if (rtsFragment != null) {
            rtsFragment.onStatusNotify();
        }
    }
}
