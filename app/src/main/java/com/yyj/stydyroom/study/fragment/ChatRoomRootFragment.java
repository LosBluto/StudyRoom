package com.yyj.stydyroom.study.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatResCode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.video.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatScreenCapturer;
import com.netease.nimlib.sdk.avchat.video.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.http.MyServer;
import com.yyj.stydyroom.base.ui.TFragment;
import com.yyj.stydyroom.base.ui.dialog.EasyAlertDialogHelper;
import com.yyj.stydyroom.permission.MPermission;
import com.yyj.stydyroom.permission.annotation.OnMPermissionDenied;
import com.yyj.stydyroom.permission.annotation.OnMPermissionGranted;
import com.yyj.stydyroom.permission.annotation.OnMPermissionNeverAskAgain;
import com.yyj.stydyroom.permission.util.MPermissionUtil;
import com.yyj.stydyroom.study.adapter.ChatRoomTabPagerAdapter;
import com.yyj.stydyroom.study.fragment.tab.ChatRoomBottomTab;
import com.yyj.stydyroom.study.fragment.tab.ChatRoomTopTab;
import com.yyj.stydyroom.study.fragment.tab.ShareScreenTabFragmentAbs;
import com.yyj.stydyroom.study.helper.ChatRoomMemberCache;
import com.yyj.stydyroom.study.helper.MsgHelper;
import com.yyj.stydyroom.study.helper.VideoListener;
import com.yyj.stydyroom.study.model.SimpleAVChatStateObserver;
import com.yyj.stydyroom.study.ui.tab.FadeInOutPageTransformer;
import com.yyj.stydyroom.study.ui.tab.PagerSlidingTabStrip;
import com.yyj.stydyroom.study.ui.tab.reminder.ReminderId;
import com.yyj.stydyroom.study.ui.tab.reminder.ReminderItem;
import com.yyj.stydyroom.study.util.Interactor;
import com.yyj.stydyroom.study.util.MeetingConstant;
import com.yyj.stydyroom.study.util.MeetingOptCommand;
import com.yyj.stydyroom.study.util.NonScrollViewPager;
import com.yyj.stydyroom.study.util.ShareType;
import com.yyj.stydyroom.views.data.MyCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChatRoomRootFragment extends TFragment implements View.OnClickListener {

    private final static String TAG = "ChatRoomRootFragment";

    private final static String ROOM_INFO = "room_info";

    private final static String IS_CREATE = "is_create";

    //允许有权限成员的最大人数
    private final static int MAX_PERMISSION_COUNT = 4;

    private static final int LIVE_PERMISSION_REQUEST_CODE = 100;

    private static final String[] LIVE_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
                                                                  Manifest.permission.RECORD_AUDIO};

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 11;

    private Intent mShareScreenIntent;

    private Activity activity;

    private ChatRoomInfo roomInfo;

    private String masterAccount;

    private String selfAccount;

    private String roomId;

    private boolean isPermissionInit = false; // 是否收到其他成员权限

    private boolean isMaster = false; // 是否是主播

    private VideoListener videoListener;

    private AVChatCameraCapturer cameraCapturer; // 视频采集模块

    private ArrayList<Interactor> interactionList;//有交互权限成员


    private TextView tvRoomIdText;

    // 开/关 相机
    private ImageView videoMuteSwitchBtn;

    // 开/关 声音
    private ImageView audioMuteSwitchBtn;

    private TextView tvStatusText;

    private Button btnCloseStudentShare;

    // 顶部tab : 老师的视频、屏幕共享、白板
    private NonScrollViewPager topViewPager;

    private ChatRoomTabPagerAdapter topAdapter;

    private int topScrollState;


    // 中间的小画布 ， 第一个是老师的，最多3个互动学生，一共4个
    private List<ViewGroup> smallVideoViewList;


    // 下面的tab : 讨论 、成员
    private PagerSlidingTabStrip bottomTabs;

    private NonScrollViewPager bottomViewPager;

    private ChatRoomTabPagerAdapter bottomAdapter;

    private int bottomScrollState;

    private TextView tvShareScreen;

    private TextView tvShowVideo;

    private TextView tvWhiteBoard;

    // 请求互动 / 结束按钮
    private TextView interactionStartCloseBtn;

    // 正在屏幕共享的人
    private Interactor sharingScreenInteractor;

    // 加入音视频房间的人
    private HashSet<String> accountsJoinedAVChannel = new HashSet<>();

    // 新获取权限的人
    private HashSet<String> accountsNewPermission = new HashSet<>();

    private ShareType preShareType = ShareType.VIDEO;

    private String preUIState;

    private boolean preOnLine = true;

    private boolean isReLogin = false;

    public static ChatRoomRootFragment newInstance(ChatRoomInfo roomInfo, boolean isCreate) {
        ChatRoomRootFragment fragment = new ChatRoomRootFragment();
        Bundle args = new Bundle();
        args.putSerializable(ROOM_INFO, roomInfo);
        args.putBoolean(IS_CREATE, isCreate);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chat_room_fragment, container, false);
        activity = getActivity();
        videoListener = (VideoListener) activity;
        if (!parseRoomInfo()) {
            return rootView;
        }
        findViews();
        registerObservers(true);
        setupTopPager();
        setupBottomPager();
        setupBottomTabs();
        setupRtc();
        updateControlUI(ShareType.VIDEO);
        updateStudentHandsUpUI();
        updateVideoAudioMuteSwitchUI();
        requestLivePermission();
        tryRequestAllPermissionMembers();
        return rootView;
    }


    private boolean parseRoomInfo() {
        Bundle args = getArguments();
        if (args != null) {
            roomInfo = (ChatRoomInfo) args.getSerializable(ROOM_INFO);
            isMaster = args.getBoolean(IS_CREATE, false);
            if (roomInfo != null) {
                roomId = roomInfo.getRoomId();
                masterAccount = roomInfo.getCreator();
                Log.i(TAG,masterAccount);
                selfAccount = MyCache.getAccount();
                return true;
            }
        }
        Toast.makeText(activity, "获取房间信息失败", Toast.LENGTH_LONG).show();
        activity.finish();
        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(activity, "你拒绝了分享的权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isShareScreen()) {
            Toast.makeText(activity, "有人在屏幕共享，不支持切换", Toast.LENGTH_SHORT).show();
            return;
        }
        mShareScreenIntent = data;
        if (isMaster) {
            sharingScreenInteractor = findMasterInteractor();
            sharingScreenInteractor.setCapturerType(Interactor.Status.SHARE_SCREEN);
            resetVideoCapturer(sharingScreenInteractor);
            updateControlUI(ShareType.VIDEO);
            MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, masterAccount, roomId);

        } else {
            MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.SHARE_SCREEN.getValue(),
                                                              masterAccount, null);
            changeSelfCapturerType(Interactor.Status.SHARE_SCREEN);
        }

    }

    // 向所有人请求成员权限
    private void requestPermissionMembers() {
        MsgHelper.getInstance().sendCustomMsg(roomId, MeetingOptCommand.GET_STATUS);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObservers(false);
        if (roomId != null) {
            NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
            clearChatRoom();
        }
    }

    public void onBackPressed() {
        logoutChatRoom();
    }

    public void onKickOut() {
        Log.d(TAG, "chat room do kick out");
        activity.finish();
    }

    private void logoutChatRoom() {
        EasyAlertDialogHelper.createOkCancelDialog(activity, null, getString(R.string.logout_confirm),
                                                   getString(R.string.leave), getString(R.string.cancel), true,
                                                   new EasyAlertDialogHelper.OnDialogActionListener() {

                                                       @Override
                                                       public void doCancelAction() {
                                                       }

                                                       @Override
                                                       public void doOkAction() {
                                                           AVChatManager.getInstance().stopVideoPreview();
                                                           AVChatManager.getInstance().disableVideo();
                                                           Log.i(TAG,"compare "+masterAccount+"  "+selfAccount);
                                                           if (masterAccount.equals(selfAccount)) {
                                                               Log.i(TAG,"CLOSE ROOM");
                                                               // 自己是老师，则关闭聊天室
                                                               closeChatRoom();
                                                           }
                                                           activity.finish();
                                                       }
                                                   }).show();

    }

    // 关闭聊天室
    private void closeChatRoom() {
        MyServer.getInstance().closeRoom(masterAccount, roomId,
                                                   new MyServer.MyCallBack<String>() {

                                                       @Override
                                                       public void onSuccess(String s) {
                                                           Log.d(TAG, "close room success");
                                                       }

                                                       @Override
                                                       public void onFailed(int code, String errorMsg) {
                                                           Log.d(TAG,
                                                                     "close room failed, code:" + code + ", errorMsg:" +
                                                                     errorMsg);
                                                       }
                                                   });
    }

    private void clearChatRoom() {
        Log.d(TAG, "chat room do clear");
        AVChatManager.getInstance().leaveRoom2(roomId, new AVChatCallback<Void>() {

            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "leave channel success");
            }

            @Override
            public void onFailed(int i) {
                Log.d(TAG, "leave channel failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
            }
        });
        AVChatManager.getInstance().disableRtc();
        ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
    }


    private void findViews() {
        interactionList = new ArrayList<>();
        smallVideoViewList = new ArrayList<>(MAX_PERMISSION_COUNT);
        View topBar = findView(R.id.chat_room_top_bar);
        tvRoomIdText = topBar.findViewById(R.id.tv_room_id);
        tvStatusText = topBar.findViewById(R.id.tv_online_status);
        topBar.findViewById(R.id.iv_back_arrow).setOnClickListener(this);
        videoMuteSwitchBtn = topBar.findViewById(R.id.video_mute_switch_btn);
        audioMuteSwitchBtn = topBar.findViewById(R.id.audio_mute_switch_btn);
        btnCloseStudentShare = topBar.findViewById(R.id.btn_close_student_share);
        videoMuteSwitchBtn.setOnClickListener(this);
        audioMuteSwitchBtn.setOnClickListener(this);
        btnCloseStudentShare.setOnClickListener(this);
        View topLayout = findView(R.id.chat_room_fragment_top);
        topViewPager = topLayout.findViewById(R.id.new_chat_room_viewpager_top);
        tvShareScreen = topLayout.findViewById(R.id.tv_share_screen);
        tvShowVideo = topLayout.findViewById(R.id.tv_show_video);
        tvWhiteBoard = topLayout.findViewById(R.id.tv_white_board);
        interactionStartCloseBtn = topLayout.findViewById(R.id.btn_apply_or_close_interaction);
        tvShareScreen.setOnClickListener(this);
        tvShowVideo.setOnClickListener(this);
        tvWhiteBoard.setOnClickListener(this);
        interactionStartCloseBtn.setOnClickListener(this);
        View cameraCapturerLayout = findView(R.id.chat_room_fragment_camera_capturer);
        // 老师的小画布显示区域
        ViewGroup masterVideoLayout = cameraCapturerLayout.findViewById(R.id.master_video_layout);
        // 第一个学生显示区域
        ViewGroup firstVideoLayout = cameraCapturerLayout.findViewById(R.id.first_video_layout);
        // 第二个学生显示区域
        ViewGroup secondVideoLayout = cameraCapturerLayout.findViewById(R.id.second_video_layout);
        // 第三个学生显示区域
        ViewGroup thirdVideoLayout = cameraCapturerLayout.findViewById(R.id.third_video_layout);
        smallVideoViewList.add(masterVideoLayout);
        smallVideoViewList.add(firstVideoLayout);
        smallVideoViewList.add(secondVideoLayout);
        smallVideoViewList.add(thirdVideoLayout);
        ///////////////////////////////聊天|成员列表切换区域///////////////////////////////////////////////////////
        bottomTabs = findView(R.id.chat_room_chat_tabs);
        bottomViewPager = findView(R.id.new_chat_room_viewpager_bottom);

    }

    public void setupRtc() {
        tvRoomIdText.setText(String.format("房间:%s", roomId));
        // 开启音视频引擎
        final AVChatManager instance = AVChatManager.getInstance();
        instance.enableRtc();
        // 打开视频模块
        instance.enableVideo();
        // 如果是主播, 设置本地预览画布
        if (isMaster) {
            Interactor masterInteractor = new Interactor(masterAccount, activity, Interactor.Status.CAMERA);
            interactionList.add(masterInteractor);
            cameraCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true);
            instance.setupVideoCapturer(cameraCapturer);
            instance.setupLocalVideoRender(masterInteractor.getRenderer(), false,
                                           AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            instance.startVideoPreview();
            instance.setParameter(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
            ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId, masterAccount);
            ChatRoomMemberCache.getInstance().setRTSOpen(true);
        } else {
            instance.setParameter(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.AUDIENCE);
        }
        instance.setParameter(AVChatParameters.KEY_SERVER_AUDIO_RECORD, true);
        instance.setParameter(AVChatParameters.KEY_SERVER_VIDEO_RECORD, true);
        instance.joinRoom2(roomId, AVChatType.VIDEO, new AVChatCallback<AVChatData>() {

            @Override
            public void onSuccess(AVChatData avChatData) {
                if (isDestroyed()) {
                    return;
                }
                if (videoListener != null) {
                    videoListener.onAVChatData(avChatData);
                }
                // 设置音量信号监听, 通过AVChatStateObserver的onReportSpeaker回调音量大小
                AVChatParameters avChatParameters = new AVChatParameters();
                avChatParameters.setBoolean(AVChatParameters.KEY_AUDIO_REPORT_SPEAKER, true);
                instance.setParameters(avChatParameters);
            }

            @Override
            public void onFailed(int i) {
                if (isDestroyed()) {
                    return;
                }
                Log.e(TAG, "join channel failed, code:" + i);
                Toast.makeText(activity, "join channel failed, code:" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable throwable) {
            }
        });

    }


    private void updateControlUI(ShareType shareType) {
        preShareType = shareType;
        if (isMaster) {
            if (isShareScreen() || shareType == ShareType.WHITE_BOARD) {
                tvShareScreen.setVisibility(View.GONE);
                tvShowVideo.setVisibility(View.VISIBLE);
            } else {
                tvShareScreen.setVisibility(View.VISIBLE);
                tvShowVideo.setVisibility(View.GONE);
            }
            btnCloseStudentShare.setVisibility(isStudentShareScreen() ? View.VISIBLE : View.GONE);
            tvWhiteBoard.setVisibility(shareType == ShareType.WHITE_BOARD ? View.GONE : View.VISIBLE);
            return;
        }
        if (ChatRoomMemberCache.getInstance().hasPermission(roomId, selfAccount)) {
            tvShareScreen.setVisibility(isShareScreen() ? View.GONE : View.VISIBLE);
            tvShowVideo.setVisibility(isSelfShareScreen() ? View.VISIBLE : View.GONE);
            videoMuteSwitchBtn.setVisibility(View.VISIBLE);
            audioMuteSwitchBtn.setVisibility(View.VISIBLE);
            interactionStartCloseBtn.setVisibility(View.VISIBLE);
            interactionStartCloseBtn.setText(R.string.finish);
            return;
        }
        tvShareScreen.setVisibility(View.GONE);
        tvShowVideo.setVisibility(View.GONE);
        videoMuteSwitchBtn.setVisibility(View.GONE);
        audioMuteSwitchBtn.setVisibility(View.GONE);
        interactionStartCloseBtn.setVisibility(View.VISIBLE);
        interactionStartCloseBtn.setText(R.string.interaction);
        tvShareScreen.setVisibility(View.GONE);

    }


    private boolean isSelfShareScreen() {
        return isShareScreen() && TextUtils.equals(sharingScreenInteractor.getAccount(), selfAccount);
    }

    private boolean isStudentShareScreen() {
        return isShareScreen() && !TextUtils.equals(sharingScreenInteractor.getAccount(), masterAccount);
    }

    private boolean isShareScreen() {
        return sharingScreenInteractor != null;
    }

    private boolean isShareScreenAccount(String account) {
        return isShareScreen() && TextUtils.equals(sharingScreenInteractor.getAccount(), account);
    }

    public void onOnlineStatusChanged(boolean isOnline, ChatRoomInfo chatRoomInfo) {
        roomInfo = chatRoomInfo;
        if (tvStatusText == null) {
            return;
        }
        tvStatusText.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        //学生断网重连上来了
        if (!preOnLine && isOnline && !isMaster) {
            updateTopUI(roomInfo.getExtension());
        }
        isReLogin = !preOnLine && isOnline;
        preOnLine = isOnline;
    }


    private void tryRequestAllPermissionMembers() {
        if (isPermissionInit) {
            return;
        }
        postDelayed(() -> {
            if (!isPermissionInit) {
                requestPermissionMembers();
            }
        }, 5000);
    }

    private void requestLivePermission() {
        MPermission.with(ChatRoomRootFragment.this).addRequestCode(LIVE_PERMISSION_REQUEST_CODE).permissions(
                LIVE_PERMISSIONS).request();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionGranted() {
        Toast.makeText(activity, "授权成功", Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDenied() {
        List<String> deniedPermissions = MPermission.getDeniedPermissions(this, LIVE_PERMISSIONS);
        String tip = "您拒绝了权限" + MPermissionUtil.toString(deniedPermissions) + "，无法开启直播";
        Toast.makeText(activity, tip, Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionNeverAskAgain(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDeniedAsNeverAskAgain() {
        List<String> deniedPermissions = MPermission.getDeniedPermissionsWithoutNeverAskAgain(this, LIVE_PERMISSIONS);
        List<String> neverAskAgainPermission = MPermission.getNeverAskAgainPermissions(this, LIVE_PERMISSIONS);
        StringBuilder sb = new StringBuilder();
        sb.append("无法开启直播，请到系统设置页面开启权限");
        sb.append(MPermissionUtil.toString(neverAskAgainPermission));
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            sb.append(",下次询问请授予权限");
            sb.append(MPermissionUtil.toString(deniedPermissions));
        }
        Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
    }


    private void registerObservers(boolean register) {
        AVChatManager.getInstance().observeAVChatState(stateObserver, register);
        ChatRoomMemberCache.getInstance().registerMeetingControlObserver(meetingControlObserver, register);
        ChatRoomMemberCache.getInstance().registerRoomMemberChangedObserver(roomMemberChangedObserver, register);
        ChatRoomMemberCache.getInstance().registerRoomInfoChangedObserver(roomInfoChangedObserver, register);
    }


    ChatRoomMemberCache.MeetingControlObserver meetingControlObserver = new ChatRoomMemberCache.MeetingControlObserver() {

        @Override
        public void onAccept(String roomID) {
            if (checkRoom(roomID)) {
                return;
            }
            ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId, selfAccount);
            chooseSpeechType();
        }

        @Override
        public void onReject(String roomID) {
            if (videoListener != null) {
                videoListener.onReject();
            }
        }

        @Override
        public void onPermissionResponse(String roomId, List<String> accounts) {
            if (checkRoom(roomId)) {
                return;
            }
            ChatRoomMemberCache.getInstance().savePermissionMemberByIds(roomId, accounts);
            //老师重新登陆上来 , 之前是学生在屏幕共享，并且之前就是在大屏上（非白板模式），所以要保留这个学生的大屏
            if (isMaster && isReLogin && preShareType == ShareType.VIDEO) {
                Iterator<String> iterator = accounts.iterator();
                while (iterator.hasNext()) {
                    String account = iterator.next();
                    if (isShareScreenAccount(account) && !isMasterAccount(account)) {
                        iterator.remove();
                    }
                }
            }
            if (accounts.isEmpty()) {
                return;
            }
            accountsNewPermission.addAll(accounts);
            tryShowStudentSmallView();
        }

        @Override
        public void onSendMyPermission(String roomID, String toAccount) {
            if (checkRoom(roomID) || !ChatRoomMemberCache.getInstance().hasPermission(roomID, selfAccount)) {
                return;
            }
            List<String> accounts = new ArrayList<>(1);
            accounts.add(selfAccount);
            MsgHelper.getInstance().sendP2PCustomNotification(roomID, MeetingOptCommand.STATUS_RESPONSE.getValue(),
                                                              toAccount, accounts);
        }

        @Override
        public void onSaveMemberPermission(String roomID, List<String> accounts) {
            if (checkRoom(roomID)) {
                return;
            }
            isPermissionInit = true;
            onPermissionChange(accounts);
        }

        @Override
        public void onHandsUp(String roomID, String account) {
            if (checkRoom(roomID)) {
                return;
            }
            onTabChange(true);
        }

        @Override
        public void onHandsDown(String roomID, String account) {
            if (checkRoom(roomID)) {
                return;
            }
            if (ChatRoomMemberCache.getInstance().hasPermission(roomID, account)) {
                studentPermissionOff(account);
                MsgHelper.getInstance().sendCustomMsg(roomId, MeetingOptCommand.ALL_STATUS);
                shareScreenStudentPermissionOff(account);
            }
            onTabChange(false);
        }

        @Override
        public void onStatusNotify(String roomID, List<String> accounts) {
            if (checkRoom(roomID)) {
                return;
            }
            onPermissionChange(accounts);
            updateControlUI(preShareType);
            if (videoListener != null) {
                videoListener.onStatusNotify();
            }
        }

        @Override
        public void onShareScreen(String roomID, String account) {
            if (checkRoom(roomID)) {
                return;
            }
            if (sharingScreenInteractor != null) {
                Log.e(TAG,
                          "student " + account + " apply share screen , but " + sharingScreenInteractor.getAccount() +
                          " already do this");
                return;
            }
            MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, account, roomID);
        }

        @Override
        public void onCancelShare(String roomID, String account) {
            if (checkRoom(roomID)) {
                return;
            }
            if (sharingScreenInteractor == null) {
                Log.e(TAG, "student " + account + " apply cancel share screen , but no one share ");
                return;
            }
            if (!TextUtils.equals(sharingScreenInteractor.getAccount(), account)) {
                Log.e(TAG, "student " + account +
                               " apply cancel share screen , but not the small account , share account = " +
                               sharingScreenInteractor.getAccount() + ",current =" + account);
                return;
            }
            MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, null, roomID);
        }
    };


    ChatRoomMemberCache.RoomMemberChangedObserver roomMemberChangedObserver = new ChatRoomMemberCache.RoomMemberChangedObserver() {

        @Override
        public void onRoomMemberIn(ChatRoomMember member) {
            Log.i(TAG, "onRoomMemberExit , account = " + member.getAccount());
            if (isMaster) {
                // 老师自己重新进来
                if (isMasterAccount(member.getAccount())) {
                    ChatRoomMemberCache.getInstance().clearAllHandsUp(roomId);
                    // 重新向所有成员请求权限
                    requestPermissionMembers();
                } else {
                    // 当有学生进来时， 老师发送点对点通知告知其有权限的成员列表
                    MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.ALL_STATUS.getValue(),
                                                                      member.getAccount(),
                                                                      ChatRoomMemberCache.getInstance()
                                                                                         .getPermissionMembers(roomId));
                }
                return;
            }
            // 老师重新进来,学生要取消自己的举手状态
            if (isMasterAccount(member.getAccount())) {
                ChatRoomMemberCache.getInstance().saveMyHandsUpDown(roomId, false);
                updateStudentHandsUpUI();
            }

        }

        @Override
        public void onRoomMemberExit(ChatRoomMember member) {
            Log.i(TAG, "onRoomMemberExit , account = " + member.getAccount());
            studentPermissionOff(member.getAccount());
            if (isMaster) {
                // 老师要清空离开成员的举手
                ChatRoomMemberCache.getInstance().removeHandsUpMem(roomId, member.getAccount());
                //如果是学生离开，需要发通知更新一下权限列表
                if (!isMasterAccount(member.getAccount())) {
                    shareScreenStudentPermissionOff(member.getAccount());
                    MsgHelper.getInstance().sendCustomMsg(roomId, MeetingOptCommand.ALL_STATUS);
                }
            }
        }
    };

    //根据roomInfo的扩展信息更新UI
    ChatRoomMemberCache.RoomInfoChangedObserver roomInfoChangedObserver = new ChatRoomMemberCache.RoomInfoChangedObserver() {

        @Override
        public void onRoomInfoUpdate(IMMessage message) {
            ChatRoomNotificationAttachment attachment = (ChatRoomNotificationAttachment) message.getAttachment();
            if (attachment != null && attachment.getExtension() != null) {
                Map<String, Object> ext = attachment.getExtension();
                updateTopUI(ext);
            }
        }
    };


    // 选择发言方式
    private void chooseSpeechType() {
        final CharSequence[] items = {"语音", "视频"}; // 设置选择内容
        final boolean[] checkedItems = {true, true};// 设置默认选中
        String content;
        if (ChatRoomMemberCache.getInstance().isMyHandsUp(roomId)) {
            content = "老师已通过你的发言申请，\n";
        } else {
            content = "老师开通了你的发言权限，\n";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Light_Dialog_Alert);
        String title = content + "请选择发言方式：";
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
               .setPositiveButton("确定", (dialog, which) -> {
                   if (!ChatRoomMemberCache.getInstance().hasPermission(roomId, selfAccount)) {
                       return;
                   }
                   AVChatManager.getInstance().enableAudienceRole(false);
                   AVChatManager.getInstance().muteLocalAudio(!checkedItems[0]);
                   AVChatManager.getInstance().muteLocalVideo(!checkedItems[1]);
                   ChatRoomMemberCache.getInstance().setRTSOpen(true);
                   topAdapter.getItem(1).onCurrent();
                   videoListener.onAcceptConfirm();
                   updateControlUI(preShareType);
                   updateVideoAudioMuteSwitchUI();
                   Interactor selfInteractor = findInteractorByAccount(selfAccount);
                   if (selfInteractor == null) {
                       selfInteractor = new Interactor(selfAccount, activity, Interactor.Status.CAMERA);
                       interactionList.add(selfInteractor);
                   } else {
                       selfInteractor.setCapturerType(Interactor.Status.CAMERA);
                   }
                   resetVideoCapturer(selfInteractor);
                   setupRender(selfInteractor);
                   addRendererToSmallVideoView(selfInteractor);
               }).setCancelable(false).show();
    }

    private void updateVideoAudioMuteSwitchUI() {
        videoMuteSwitchBtn.setBackgroundResource(AVChatManager.getInstance()
                                                              .isLocalVideoMuted() ? R.drawable.chat_room_video_off_selector : R.drawable.chat_room_video_on_selector);
        audioMuteSwitchBtn.setBackgroundResource(AVChatManager.getInstance()
                                                              .isLocalAudioMuted() ? R.drawable.chat_room_audio_off_selector : R.drawable.chat_room_audio_on_selector);


    }


    // 举手红点提醒
    public void onTabChange(boolean notify) {
        ReminderItem item = new ReminderItem(ReminderId.CONTACT);
        item.setIndicator(notify);
        ChatRoomBottomTab tab = ChatRoomBottomTab.fromReminderId(item.getId());
        if (tab != null) {
            bottomTabs.updateTab(tab.tabIndex, item);
        }
    }


    private void setupBottomPager() {
        // 主播没有举手发言的tab
        bottomAdapter = new ChatRoomTabPagerAdapter(getFragmentManager(), activity, bottomViewPager,
                                                    ChatRoomBottomTab.values().length,
                                                    ChatRoomTabPagerAdapter.TYPE.BOTTOM);
        bottomViewPager.setOffscreenPageLimit(bottomAdapter.getCacheCount());
        bottomViewPager.setPageTransformer(true, new FadeInOutPageTransformer());
        bottomViewPager.setAdapter(bottomAdapter);
        bottomViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                bottomTabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
                bottomAdapter.onPageScrolled(position);
            }

            @Override
            public void onPageSelected(int position) {
                bottomTabs.onPageSelected(position);
                if (bottomScrollState == ViewPager.SCROLL_STATE_IDLE) {
                    bottomAdapter.onPageSelected(bottomViewPager.getCurrentItem());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                bottomTabs.onPageScrollStateChanged(state);
                bottomScrollState = state;
                if (bottomScrollState == ViewPager.SCROLL_STATE_IDLE) {
                    bottomAdapter.onPageSelected(bottomViewPager.getCurrentItem());
                }
            }
        });
    }

    private void setupTopPager() {
        // 主播没有举手发言的tab
        topAdapter = new ChatRoomTabPagerAdapter(getFragmentManager(), activity, topViewPager,
                                                 ChatRoomTopTab.values().length, ChatRoomTabPagerAdapter.TYPE.TOP);
        topViewPager.setOffscreenPageLimit(topAdapter.getCacheCount());
        topViewPager.setPageTransformer(true, new FadeInOutPageTransformer());
        topViewPager.setAdapter(topAdapter);
        topViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // TO ADAPTER
                topAdapter.onPageScrolled(position);
            }

            @Override
            public void onPageSelected(int position) {
                if (topScrollState == ViewPager.SCROLL_STATE_IDLE) {
                    topAdapter.onPageSelected(topViewPager.getCurrentItem());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                topScrollState = state;
                if (topScrollState == ViewPager.SCROLL_STATE_IDLE) {
                    topAdapter.onPageSelected(topViewPager.getCurrentItem());
                }
            }
        });
    }


    private void setupBottomTabs() {
        bottomTabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {

            @Override
            public int getTabLayoutResId(int position) {
                return R.layout.chat_room_tab_layout;
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        bottomTabs.setViewPager(bottomViewPager);
        bottomTabs.setOnTabClickListener(bottomAdapter);
        bottomTabs.setOnTabDoubleTapListener(bottomAdapter);
    }


    protected AVChatStateObserver stateObserver = new SimpleAVChatStateObserver() {

        @Override
        public void onLeaveChannel() {
            hideInteractor(selfAccount);
        }

        @Override
        public void onJoinedChannel(int result, String s, String s1, int i1) {
            if (result != AVChatResCode.JoinChannelCode.OK) {
                Toast.makeText(activity, "joined channel:" + result, Toast.LENGTH_SHORT).show();
                return;
            }
            // 老师自己加入频道后，开启预览
            if (isMaster) {
                Map<String, Object> ext = new HashMap<>();
                ext.put(MeetingConstant.SHOW_TYPE, ShareType.VIDEO.getValue());
                updateTopUI(ext);
            }

        }

        @Override
        public void onUserJoined(String account) {
            accountsJoinedAVChannel.add(account);
            if (isMasterAccount(account)) {
                onMasterJoin();
            } else {
                tryShowStudentSmallView();
            }
        }


        @Override
        public void onFirstVideoFrameAvailable(String account) {
            Log.i(TAG, "onFirstVideoFrameAvailable = " + account);
        }

        @Override
        public void onUserLeave(String account, int i) {
            accountsJoinedAVChannel.remove(account);
            if (isMasterAccount(account)) {
                onMasterLeave();
            } else {
                studentPermissionOff(account);
            }

        }

        @Override
        public void onReportSpeaker(Map<String, Integer> map, int i) {
            videoListener.onReportSpeaker(map);
        }

    };


    // 老师进入频道
    private void onMasterJoin() {
        if (isMaster) {
            return;
        }
        Interactor masterInteractor = findInteractorByAccount(masterAccount);
        if (masterInteractor != null) {
            return;
        }
        masterInteractor = new Interactor(masterAccount, activity, Interactor.Status.CAMERA);
        interactionList.add(masterInteractor);
        setupRender(masterInteractor);
        ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId, masterAccount);
        updateTopUI(roomInfo.getExtension());
    }


    // 老师离开频道
    private void onMasterLeave() {
        if (isDestroyed()) {
            return;
        }
        Toast.makeText(activity, "老师退出了房间", Toast.LENGTH_SHORT).show();
        activity.finish();
    }


    // 将被取消权限的成员从画布移除, 并将角色置为初始状态
    private void studentPermissionOff(String account) {
        accountsNewPermission.remove(account);
        if (isMasterAccount(account)) {
            return;
        }
        if (!ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
            return;
        }
        ChatRoomMemberCache.getInstance().removePermissionMember(roomId, account);
        hideInteractor(account);
        if (TextUtils.equals(account, selfAccount)) {
            changeSelfToAudience();
        }
    }


    public void closeStudentPermission(String account) {
        studentPermissionOff(account);
        shareScreenStudentPermissionOff(account);
    }


    private void shareScreenStudentPermissionOff(String account) {
        if (!isShareScreenAccount(account)) {
            return;
        }
        // 如果现在不是视频而是白板，不能更新
        if (preShareType == ShareType.VIDEO) {
            MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, null, roomId);
        }
    }


    public void onKickOutSuccess(String account) {
        if (!ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
            return;
        }
        closeStudentPermission(account);
        // 通知更新的有权限的成员列表
        MsgHelper.getInstance().sendCustomMsg(roomId, MeetingOptCommand.ALL_STATUS);
    }


    private void changeSelfToAudience() {
        Log.i(TAG, "changeSelfToAudience...");
        AVChatManager.getInstance().enableAudienceRole(true);
        ChatRoomMemberCache.getInstance().setRTSOpen(false);
        videoListener.onLineFragNotify();
    }

    public void studentPermissionOn(String account) {
        // 更新本地权限缓存, 添加新的成员
        ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId, account);
        if (isMasterAccount(account)) {
            return;
        }
        accountsNewPermission.add(account);
        tryShowStudentSmallView();
    }


    //切换当前页面top部分的ui
    private void updateTopUI(Map<String, Object> ext) {
        if (ext == null || !ext.containsKey(MeetingConstant.SHOW_TYPE)) {
            return;
        }
        int type = (int) ext.get(MeetingConstant.SHOW_TYPE);
        String shareId = (String) ext.get(MeetingConstant.SHARE_ID);
        String uiState = type + "#" + shareId;
        Log.i(TAG, "updateTopUI , ext = " + uiState);
        if (TextUtils.equals(preUIState, uiState)) {
            Log.e(TAG, "ui state is equal , uiState = " + uiState);
            return;
        }
        preUIState = uiState;
        if (type == ShareType.VIDEO.getValue()) {
            if (topViewPager.getCurrentItem() != 0) {
                topViewPager.setCurrentItem(0);
            }
            if (TextUtils.isEmpty(shareId)) {
                Interactor masterInteractor = findMasterInteractor();
                masterInteractor.setCapturerType(Interactor.Status.CAMERA);
                setBigPreviewLayout(masterInteractor);
            } else {
                //在shareId不为空的情况，把有权限的录屏放到大屏
                sharingScreenInteractor = findInteractorByAccount(shareId);
                if (sharingScreenInteractor == null) {
                    sharingScreenInteractor = new Interactor(shareId, activity, Interactor.Status.SHARE_SCREEN);
                } else {
                    sharingScreenInteractor.setCapturerType(Interactor.Status.SHARE_SCREEN);
                }
                setBigPreviewLayout(sharingScreenInteractor);
            }
        } else if (type == ShareType.WHITE_BOARD.getValue()) {
            if (topViewPager.getCurrentItem() != 1) {
                topViewPager.setCurrentItem(1);
            }
            //1. 切到白板时，把老师放到小屏去，用来显示相机画面
            Interactor masterInteractor = findMasterInteractor();
            addRendererToSmallVideoView(masterInteractor);
            // 2. 切到白板时，如果有之前是学生在共享，放到小屏去，用来显示相机画面
            if (sharingScreenInteractor != null && sharingScreenInteractor != masterInteractor) {
                addRendererToSmallVideoView(sharingScreenInteractor);
            }
            //3. 一旦到了白板，屏幕共享都会切成相机
            judgeJustCloseStudentShare();
            sharingScreenInteractor = null;
        }
        updateControlUI(ShareType.statusOfValue(type));
    }

    private void setupRender(Interactor interactor) {
        try {
            AVChatManager instance = AVChatManager.getInstance();
            if (interactor.getAccount().equals(selfAccount)) {
                instance.setupLocalVideoRender(null, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
                instance.setupLocalVideoRender(interactor.getRenderer(), false,
                                               AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            } else {
                instance.setupRemoteVideoRender(interactor.getAccount(), null, false,
                                                AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
                instance.setupRemoteVideoRender(interactor.getAccount(), interactor.getRenderer(), false,
                                                AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            }
        } catch (Exception e) {
            Log.e(TAG, "set up video render error:" + e.getMessage());
            e.printStackTrace();
        }

    }


    // 权限变化
    public void onPermissionChange(List<String> newAccounts) {
        List<String> oldAccounts = ChatRoomMemberCache.getInstance().getPermissionMembers(roomId);
        if (oldAccounts == null) {
            oldAccounts = new ArrayList<>();
        }
        for (String oldAccount : oldAccounts) {
            if (oldAccount.equals(masterAccount)) {
                continue;
            }
            // 权限被取消
            if (!newAccounts.contains(oldAccount)) {
                studentPermissionOff(oldAccount);
            }
        }
        newAccounts.removeAll(oldAccounts);
        accountsNewPermission.addAll(newAccounts);
        ChatRoomMemberCache.getInstance().savePermissionMemberByIds(roomId, newAccounts);
        tryShowStudentSmallView();
    }


    //显示互动学生的小画布（包含自己的）
    private void tryShowStudentSmallView() {
        Iterator<String> newPermissionIterator = accountsNewPermission.iterator();
        while (newPermissionIterator.hasNext()) {
            String newPermissionAccount = newPermissionIterator.next();
            if (accountsJoinedAVChannel.contains(newPermissionAccount)) {
                newPermissionIterator.remove();
                if (isMasterAccount(newPermissionAccount)) {
                    continue;
                }
                Interactor interactor = findInteractorByAccount(newPermissionAccount);
                if (interactor == null) {
                    interactor = new Interactor(newPermissionAccount, activity, Interactor.Status.CAMERA);
                    interactionList.add(interactor);
                }
                setupRender(interactor);
                addRendererToSmallVideoView(interactor);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_arrow: {
                onBackPressed();
                break;
            }
            case R.id.video_mute_switch_btn: {
                switchMuteVideo();
                break;
            }
            case R.id.audio_mute_switch_btn: {
                switchMuteAudio();
                break;
            }
            case R.id.btn_apply_or_close_interaction: {
                studentApplyOrCloseInteraction();
                break;
            }
            case R.id.btn_close_student_share: {
                closeStudentShare();
                break;
            }
            case R.id.tv_white_board: {
                masterShareWhiteBoard();
                break;
            }
            case R.id.tv_share_screen: {
                shareScreen();
                break;
            }
            case R.id.tv_show_video: {
                showVideo();
                break;
            }
        }
    }

    private void masterShareWhiteBoard() {
        //大屏切白板
        if (preShareType != ShareType.VIDEO) {
            return;
        }
        // 如果是老师自己在屏幕共享，小屏切换到视频
        if (isSelfShareScreen()) {
            Interactor masterInteractor = findMasterInteractor();
            masterInteractor.setCapturerType(Interactor.Status.CAMERA);
            resetVideoCapturer(masterInteractor);
        }
        MsgHelper.getInstance().updateRoomInfo(ShareType.WHITE_BOARD, null, roomId);
    }


    private void showVideo() {
        if (AVChatManager.getInstance().isLocalVideoMuted()) {
            Toast.makeText(activity, "请先开启发送视频", Toast.LENGTH_SHORT).show();
            return;
        }
        //关闭屏幕共享
        if (isMaster) {
            if (isSelfShareScreen()) {
                Interactor masterInteractor = findMasterInteractor();
                masterInteractor.setCapturerType(Interactor.Status.CAMERA);
                resetVideoCapturer(masterInteractor);
            }
            MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, null, roomId);

        } else {
            MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.CANCEL_SHARE_SCREEN.getValue(),
                                                              masterAccount, null);
            changeSelfCapturerType(Interactor.Status.CAMERA);
        }

    }

    private void shareScreen() {
        if (AVChatManager.getInstance().isLocalVideoMuted()) {
            Toast.makeText(activity, "请先开启发送视频", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isShareScreen()) {
            Toast.makeText(activity, "有人在屏幕共享，不支持切换", Toast.LENGTH_SHORT).show();
            return;
        }
        // 如果之前没有人在屏幕共享，则开启屏幕共享
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(activity, "手机版本过低，不支持屏幕共享", Toast.LENGTH_SHORT).show();
            return;
        }
        tryStartScreenCapturer();

    }


    // 学生申请上麦或主动下麦
    private void studentApplyOrCloseInteraction() {
        // 结束互动
        if (ChatRoomMemberCache.getInstance().hasPermission(roomId, selfAccount)) {
            cancelInteractionConfirm();
            return;
        }
        // 取消 申请互动
        if (ChatRoomMemberCache.getInstance().isMyHandsUp(roomId)) {
            MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.SPEAK_REQUEST_CANCEL.getValue(),
                                                              masterAccount, null);
            ChatRoomMemberCache.getInstance().saveMyHandsUpDown(roomId, false);
            updateStudentHandsUpUI();
            return;
        }
        // 申请互动
        MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.SPEAK_REQUEST.getValue(),
                                                          masterAccount, null);
        ChatRoomMemberCache.getInstance().saveMyHandsUpDown(roomId, true);
        updateStudentHandsUpUI();
        return;
    }

    //  关闭学生的屏幕共享
    private void closeStudentShare() {
        MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, null, roomId);
    }

    private void cancelInteractionConfirm() {
        String message = getString(R.string.exit_interaction);
        String title = getString(R.string.operation_confirm);
        String okStr = getString(R.string.exit);
        EasyAlertDialogHelper.createOkCancelDialog(activity, title, message, okStr, getString(R.string.cancel), true,
                                                   new EasyAlertDialogHelper.OnDialogActionListener() {

                                                       @Override
                                                       public void doCancelAction() {
                                                       }

                                                       @Override
                                                       public void doOkAction() {
                                                           if (videoListener != null) {
                                                               videoListener.onReject();
                                                           }
                                                           MsgHelper.getInstance().sendP2PCustomNotification(roomId,
                                                                                                             MeetingOptCommand.SPEAK_REQUEST_CANCEL
                                                                                                                     .getValue(),
                                                                                                             masterAccount,
                                                                                                             null);
                                                           ChatRoomMemberCache.getInstance().saveMyHandsUpDown(roomId,
                                                                                                               false);
                                                           ChatRoomMemberCache.getInstance().removePermissionMember(
                                                                   roomId, selfAccount);
                                                           changeSelfToAudience();
                                                           updateStudentHandsUpUI();
                                                           Interactor selfInteractor = findInteractorByAccount(
                                                                   selfAccount);
                                                           selfInteractor.release();
                                                       }
                                                   }).show();
    }

    // 设置自己的摄像头是否开启
    private void switchMuteVideo() {
        if (AVChatManager.getInstance().isLocalVideoMuted()) {
            videoMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_video_on_selector);
            AVChatManager.getInstance().muteLocalVideo(false);
        } else {
            videoMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_video_off_selector);
            AVChatManager.getInstance().muteLocalVideo(true);
        }
    }

    // 设置自己的录音是否开启
    private void switchMuteAudio() {
        if (AVChatManager.getInstance().isLocalAudioMuted()) {
            audioMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_audio_on_selector);
            AVChatManager.getInstance().muteLocalAudio(false);
        } else {
            audioMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_audio_off_selector);
            AVChatManager.getInstance().muteLocalAudio(true);
        }
    }


    private void updateStudentHandsUpUI() {
        if (isMaster) {
            return;
        }
        // 主播通过，进行互动
        if (ChatRoomMemberCache.getInstance().hasPermission(roomId, selfAccount)) {
            interactionStartCloseBtn.setText(R.string.finish);
        } else if (!ChatRoomMemberCache.getInstance().isMyHandsUp(roomId)) {
            // 没举手
            interactionStartCloseBtn.setText(R.string.interaction);
        }
        // 举手等待主播通过
        else if (ChatRoomMemberCache.getInstance().isMyHandsUp(roomId) &&
                 !ChatRoomMemberCache.getInstance().hasPermission(roomId, selfAccount)) {
            interactionStartCloseBtn.setText(R.string.cancel);

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void tryStartScreenCapturer() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getActivity().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, CAPTURE_PERMISSION_REQUEST_CODE);
    }


    private void changeSelfCapturerType(int newType) {
        Interactor selfInteractor = findInteractorByAccount(selfAccount);
        selfInteractor.setCapturerType(newType);
        resetVideoCapturer(selfInteractor);
    }

    /**
     * 切换当前用户视频类型
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void resetVideoCapturer(Interactor interactor) {
        AVChatManager.getInstance().stopVideoPreview();
        int capturerType = interactor.getCapturerType();
        // 录屏采集模块
        AVChatScreenCapturer screenCapturer;
        switch (capturerType) {
            case Interactor.Status.CAMERA: {
                if (cameraCapturer == null) {
                    cameraCapturer = AVChatVideoCapturerFactory.createCameraCapturer(true);
                }
                AVChatManager.getInstance().setupVideoCapturer(cameraCapturer);
                screenCapturer = null;
                break;
            }
            case Interactor.Status.SHARE_SCREEN: {
                screenCapturer = AVChatVideoCapturerFactory.createScreenVideoCapturer(mShareScreenIntent,
                                                                                      new MediaProjection.Callback() {

                                                                                          @Override
                                                                                          public void onStop() {
                                                                                              super.onStop();
                                                                                          }
                                                                                      });
                AVChatManager.getInstance().setupVideoCapturer(screenCapturer);
                break;
            }
        }
        AVChatManager.getInstance().startVideoPreview();
    }


    /**
     * @param interactor 大屏即将展示的页面
     */
    private void setBigPreviewLayout(Interactor interactor) {
        interactor.removeFromParent();
        // 如果不是屏幕共享
        if (sharingScreenInteractor == null || interactor.getCapturerType() != Interactor.Status.SHARE_SCREEN) {
            judgeJustCloseStudentShare();
            sharingScreenInteractor = null;
        }
        //屏幕共享
        else {
            if (isStudentShareScreen()) {
                Interactor masterInteractor = findMasterInteractor();
                addRendererToSmallVideoView(masterInteractor);
            }
        }
        setBigView(interactor);
    }


    private void judgeJustCloseStudentShare() {
        //之前是学生做的屏幕共享 ， 现在关闭了，如果还有互动权限先从大屏上移除，放到小屏上
        if (isStudentShareScreen() && ChatRoomMemberCache.getInstance().hasPermission(roomId, sharingScreenInteractor
                .getAccount())) {
            //如果之前是自己做的屏幕共享，现在被老师切掉了，需要切换到视频去
            if (isSelfShareScreen()) {
                changeSelfCapturerType(Interactor.Status.CAMERA);
            }
            addRendererToSmallVideoView(sharingScreenInteractor);
        }
    }


    private boolean checkRoom(String roomID) {
        return TextUtils.isEmpty(roomId) || !roomId.equals(roomID);
    }


    public boolean isMasterAccount(String account) {
        return TextUtils.equals(account, masterAccount);
    }

    private Interactor findMasterInteractor() {
        Interactor masterInteractor = findInteractorByAccount(masterAccount);
        if (masterInteractor == null) {
            masterInteractor = new Interactor(masterAccount, activity, Interactor.Status.CAMERA);
            interactionList.add(masterInteractor);
            setupRender(masterInteractor);
            Log.e(TAG, "findMasterInteractor , but null , so create one");
        }
        return masterInteractor;
    }

    private Interactor findInteractorByAccount(String account) {
        for (Interactor interactor : interactionList) {
            if (TextUtils.equals(interactor.getAccount(), account)) {
                return interactor;
            }
        }
        return null;
    }


    private void hideInteractor(String account) {
        Interactor interactor = findInteractorByAccount(account);
        if (interactor != null) {
            interactor.release();
        }

    }

    private void addRendererToSmallVideoView(Interactor interactor) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.MATCH_PARENT);
        interactor.removeFromParent();
        //老师的放在第一个
        if (isMasterAccount(interactor.getAccount())) {
            smallVideoViewList.get(0).addView(interactor.getRenderer(), params);
            return;
        }
        // 学生从第二个开始放
        for (int i = 1; i < smallVideoViewList.size(); i++) {
            if (smallVideoViewList.get(i).getChildCount() == 0) {
                smallVideoViewList.get(i).addView(interactor.getRenderer(), params);
                return;
            }
        }
        Log.e(TAG, "error , not find empty small video view , id =  " + interactor.getAccount());
    }


    private void setBigView(Interactor interactor) {
        ((ShareScreenTabFragmentAbs) (topAdapter.getItem(0))).addVideoView(interactor.getRenderer());
    }


}
