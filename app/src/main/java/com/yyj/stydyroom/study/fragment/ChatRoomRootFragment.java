package com.yyj.stydyroom.study.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
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


import com.netease.lava.nertc.sdk.NERtc;
import com.netease.lava.nertc.sdk.NERtcCallback;
import com.netease.lava.nertc.sdk.NERtcCallbackEx;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver;
import com.netease.lava.nertc.sdk.video.NERtcRemoteVideoStreamType;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
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
import com.yyj.stydyroom.study.util.enums.RoomType;
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

    private final static String ROOM_TYPE = "room_type";

    private final static String IS_CREATE = "is_create";

    //允许有权限成员的最大人数
    private final static int MAX_PERMISSION_COUNT = 4;

    private static final int LIVE_PERMISSION_REQUEST_CODE = 100;

    private static final String[] LIVE_PERMISSIONS = new String[]{Manifest.permission.CAMERA,
                                                                  Manifest.permission.RECORD_AUDIO};

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 11;


    private Activity activity;

    private ChatRoomInfo roomInfo;

    private String masterAccount;

    private String selfAccount;

    private String roomId;

    private int roomType;

    private boolean isPermissionInit = false; // 是否收到其他成员权限

    private boolean isMaster = false; // 是否是主播

    private VideoListener videoListener;


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
    private List<NERtcVideoView> mRemoteUserVvList;


    // 下面的tab : 讨论 、成员
    private PagerSlidingTabStrip bottomTabs;

    private NonScrollViewPager bottomViewPager;

    private ChatRoomTabPagerAdapter bottomAdapter;

    private int bottomScrollState;

    private TextView tvShareScreen;

    private TextView tvShowVideo;

    // 加入音视频房间的人
    private HashSet<String> roomMember = new HashSet<>(MAX_PERMISSION_COUNT);


    private ShareType preShareType = ShareType.VIDEO;

    private TextView tv_room_id;

    private boolean preOnLine = true;

    private boolean isReLogin = false;

    private NERtcVideoView mLocalUserVv;
    private boolean isShareScreen = false;
    private boolean muteVideo = false;
    private boolean muteAudio = false;
    View cameraCapturerLayout;

    public static ChatRoomRootFragment newInstance(ChatRoomInfo roomInfo,int roomType, boolean isCreate) {
        ChatRoomRootFragment fragment = new ChatRoomRootFragment();
        Bundle args = new Bundle();
        args.putSerializable(ROOM_INFO, roomInfo);
        args.putBoolean(IS_CREATE, isCreate);
        args.putInt(ROOM_TYPE,roomType);
        fragment.setArguments(args);
        return fragment;
    }

    private void initNERtc(){
        try {
            Log.d(TAG,"init NERtc");
            NERtcParameters parameters = new NERtcParameters();
            parameters.set(NERtcParameters.KEY_AUTO_SUBSCRIBE_AUDIO, false);
            NERtcEx.getInstance().setParameters(parameters); //先设置参数，后初始化

            NERtcEx.getInstance().init(MyCache.getContext(),"f3be95142ec02f4683e11fc0c337e1ee",callback,null);
            NERtcEx.getInstance().enableLocalAudio(true);
            NERtcEx.getInstance().enableLocalVideo(true);

            Toast.makeText(activity,"SDK初始化成功",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"初始化失败");
            Toast.makeText(activity,"SDK初始化失败",Toast.LENGTH_SHORT).show();
        }
    }


    private void joinChannel(){
        NERtcEx.getInstance().joinChannel(null,roomId, Long.parseLong(selfAccount));
        mLocalUserVv.setZOrderMediaOverlay(true);
        mLocalUserVv.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
        NERtc.getInstance().setupLocalVideoCanvas(mLocalUserVv);        //绑定view
        mLocalUserVv.setTag(selfAccount);
    }


    NERtcCallback callback = new NERtcCallback() {
        @Override
        public void onJoinChannel(int i, long l, long l1) {
            Log.d(TAG,"join channel");
            if (i != AVChatResCode.JoinChannelCode.OK) {
                Toast.makeText(activity, "joined channel:" + i, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLeaveChannel(int i) {
            NERtc.getInstance().release();
        }

        @Override
        public void onUserJoined(long l) {          //有人加入房间的话
            String account = String.valueOf(l);

            if (isMasterAccount(account)) {
                onMasterJoin();
            }
            int test = 1;
            roomMember.add(account);
            ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId,account);

            refreshCanvas(account);
        }

        @Override
        public void onUserLeave(long l, int i) {
            String account = String.valueOf(l);

            if (isMasterAccount(account)) {
                onMasterLeave();
            } else {
                memberOff(account);
            }

            roomMember.remove(account);

            NERtcVideoView userView = cameraCapturerLayout.findViewWithTag(account);
            if (userView != null) {
                userView.setTag(null);
            }

        }

        @Override
        public void onUserAudioStart(long l) {

        }

        @Override
        public void onUserAudioStop(long l) {

        }

        @Override
        public void onUserVideoStart(long l, int i) {
            NERtc.getInstance().subscribeRemoteVideoStream(l, NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh, true);
        }

        @Override
        public void onUserVideoStop(long l) {

        }

        @Override
        public void onDisconnect(int i) {
            Log.d(TAG,"disconnect");
        }
    };

    private void refreshCanvas(String account){
        for (NERtcVideoView videoView: mRemoteUserVvList){
            if (videoView.getTag() == null) {
                videoView.setZOrderMediaOverlay(true);
                videoView.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
                NERtc.getInstance().setupRemoteVideoCanvas(videoView, Long.parseLong(account));
                videoView.setTag(account);
                Log.d(TAG,"setupremoteVideo:"+account);
                break;
            }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.chat_room_fragment, container, false);
        activity = getActivity();

        initNERtc();
        videoListener = (VideoListener) activity;
        if (!parseRoomInfo()) {
            return rootView;
        }
        findViews();
        registerObservers(true);
        setupTopPager();
        setupBottomPager();
        setupBottomTabs();
        updateControlUI();
        updateVideoAudioMuteSwitchUI();
        requestLivePermission();
        tryRequestAllPermissionMembers();

        joinChannel();
        return rootView;
    }


    private boolean parseRoomInfo() {
        Bundle args = getArguments();
        if (args != null) {
            roomInfo = (ChatRoomInfo) args.getSerializable(ROOM_INFO);
            isMaster = args.getBoolean(IS_CREATE, false);
            roomType = args.getInt(ROOM_TYPE,1);
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

    /*
    非正常退出关闭聊天室
     */
    public void abnormalClose(){
        if (masterAccount.equals(selfAccount)) {
            Log.i(TAG,"CLOSE ROOM");
            // 自己是老师，则关闭聊天室
            closeChatRoom();
        }
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


    @SuppressLint("SetTextI18n")
    private void findViews() {
        mRemoteUserVvList = new ArrayList<>(MAX_PERMISSION_COUNT);
        View topBar = findView(R.id.chat_room_top_bar);
        tvStatusText = topBar.findViewById(R.id.tv_online_status);
        topBar.findViewById(R.id.iv_back_arrow).setOnClickListener(this);
        videoMuteSwitchBtn = topBar.findViewById(R.id.video_mute_switch_btn);
        audioMuteSwitchBtn = topBar.findViewById(R.id.audio_mute_switch_btn);
        btnCloseStudentShare = topBar.findViewById(R.id.btn_close_student_share);
        videoMuteSwitchBtn.setOnClickListener(this);
        audioMuteSwitchBtn.setOnClickListener(this);
        btnCloseStudentShare.setOnClickListener(this);
        tv_room_id = topBar.findViewById(R.id.tv_room_id);
        tv_room_id.setText("房间: "+roomId+" 类型: "+RoomType.getTypeByTypeId(roomType));

        View topLayout = findView(R.id.chat_room_fragment_top);
        topViewPager = topLayout.findViewById(R.id.new_chat_room_viewpager_top);
        tvShareScreen = topLayout.findViewById(R.id.tv_share_screen);
        tvShowVideo = topLayout.findViewById(R.id.tv_show_video);

        tvShareScreen.setOnClickListener(this);
        tvShowVideo.setOnClickListener(this);

        cameraCapturerLayout = findView(R.id.chat_room_fragment_camera_capturer);
        // 第一个学生显示区域
        NERtcVideoView firstVideoLayout = cameraCapturerLayout.findViewById(R.id.first_video_layout);
        // 第二个学生显示区域
        NERtcVideoView secondVideoLayout = cameraCapturerLayout.findViewById(R.id.second_video_layout);
        // 第三个学生显示区域
        NERtcVideoView thirdVideoLayout = cameraCapturerLayout.findViewById(R.id.third_video_layout);
        mRemoteUserVvList.add(firstVideoLayout);
        mRemoteUserVvList.add(secondVideoLayout);
        mRemoteUserVvList.add(thirdVideoLayout);
        for (NERtcVideoView videoView:mRemoteUserVvList){
            videoView.setOnClickListener(userVvListViewListener);
        }
        cameraCapturerLayout.setOnClickListener(this);
        ///////////////////////////////聊天|成员列表切换区域///////////////////////////////////////////////////////
        bottomTabs = findView(R.id.chat_room_chat_tabs);
        bottomViewPager = findView(R.id.new_chat_room_viewpager_bottom);

        mLocalUserVv = topLayout.findViewById(R.id.vv_local_user);      //初始化视频view
    }

    private View.OnClickListener userVvListViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null)                 //如果是为被使用的view则不会替换
                return;

            switch (v.getId()){
                case R.id.first_video_layout:{
                    String remoteTag = (String) v.getTag();             //获取tag
                    String localTag = (String) mLocalUserVv.getTag();

                    if (localTag.equals(selfAccount)) {                 //如果本地tag和self账号相同
                        NERtc.getInstance().setupLocalVideoCanvas(mRemoteUserVvList.get(0));
                        NERtc.getInstance().setupRemoteVideoCanvas(mLocalUserVv, Long.parseLong(remoteTag));

                    }else {
                        NERtc.getInstance().setupLocalVideoCanvas(mLocalUserVv);
                        NERtc.getInstance().setupRemoteVideoCanvas(mRemoteUserVvList.get(0), Long.parseLong(localTag));
                    }

                    mLocalUserVv.setTag(remoteTag);                     //交换tag
                    mRemoteUserVvList.get(0).setTag(localTag);


                    break;
                }
                case R.id.second_video_layout:{
                    String remoteTag = (String) v.getTag();
                    String localTag = (String) mLocalUserVv.getTag();

                    if (localTag.equals(selfAccount)) {                 //如果本地tag和self账号相同
                        NERtc.getInstance().setupLocalVideoCanvas(mRemoteUserVvList.get(1));
                        NERtc.getInstance().setupRemoteVideoCanvas(mLocalUserVv, Long.parseLong(remoteTag));
                    }else {
                        NERtc.getInstance().setupLocalVideoCanvas(mLocalUserVv);
                        NERtc.getInstance().setupRemoteVideoCanvas(mRemoteUserVvList.get(1), Long.parseLong(localTag));
                    }

                    mLocalUserVv.setTag(remoteTag);
                    mRemoteUserVvList.get(1).setTag(localTag);
                    break;
                }
                case R.id.third_video_layout:{
                    String remoteTag = (String) v.getTag();
                    String localTag = (String) mLocalUserVv.getTag();

                    if (localTag.equals(selfAccount)) {                 //如果本地tag和self账号相同
                        NERtc.getInstance().setupLocalVideoCanvas(mRemoteUserVvList.get(2));
                        NERtc.getInstance().setupRemoteVideoCanvas(mLocalUserVv, Long.parseLong(remoteTag));
                    }else {
                        NERtc.getInstance().setupLocalVideoCanvas(mLocalUserVv);
                        NERtc.getInstance().setupRemoteVideoCanvas(mRemoteUserVvList.get(2), Long.parseLong(localTag));
                    }

                    mLocalUserVv.setTag(remoteTag);
                    mRemoteUserVvList.get(2).setTag(localTag);
                    break;
                }
            }
        }
    };

    private void updateControlUI() {

        tvShareScreen.setVisibility(isShareScreen? View.GONE : View.VISIBLE);
        tvShowVideo.setVisibility(isShareScreen?View.VISIBLE : View.GONE);
        videoMuteSwitchBtn.setVisibility(View.VISIBLE);
        audioMuteSwitchBtn.setVisibility(View.VISIBLE);

    }

    public void onOnlineStatusChanged(boolean isOnline, ChatRoomInfo chatRoomInfo) {
        roomInfo = chatRoomInfo;
        if (tvStatusText == null) {
            return;
        }
        tvStatusText.setVisibility(isOnline ? View.GONE : View.VISIBLE);
        //学生断网重连上来了
        if (!preOnLine && isOnline && !isMaster) {

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

            }
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
        public void onStatusNotify(String roomID, List<String> accounts) {
            if (checkRoom(roomID)) {
                return;
            }
            onPermissionChange(accounts);
            updateControlUI();
            if (videoListener != null) {
                videoListener.onStatusNotify();
            }
        }

        @Override
        public void onShareScreen(String roomID, String account) {
            if (checkRoom(roomID)) {
                return;
            }
            MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, account, roomID);
        }

        @Override
        public void onCancelShare(String roomID, String account) {
            if (checkRoom(roomID)) {
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
                    // 重新向所有成员请求权限
                    requestPermissionMembers();
                } else {
                    // 当有学生进来时， 老师发送点对点通知告知其有权限的成员列表
                    MsgHelper.getInstance().sendP2PCustomNotification(roomId, MeetingOptCommand.ALL_STATUS.getValue(),
                                                                      member.getAccount(),
                                                                      ChatRoomMemberCache.getInstance()
                                                                                         .getPermissionMembers(roomId));
                }
            }


        }

        @Override
        public void onRoomMemberExit(ChatRoomMember member) {
            Log.i(TAG, "onRoomMemberExit , account = " + member.getAccount());
            memberOff(member.getAccount());
            if (isMaster) {
                //如果是学生离开，需要发通知更新一下权限列表
                if (!isMasterAccount(member.getAccount())) {
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

            }
        }
    };

    private void updateVideoAudioMuteSwitchUI() {
        videoMuteSwitchBtn.setBackgroundResource(muteVideo ? R.drawable.chat_room_video_off_selector : R.drawable.chat_room_video_on_selector);
        audioMuteSwitchBtn.setBackgroundResource(muteAudio ? R.drawable.chat_room_audio_off_selector : R.drawable.chat_room_audio_on_selector);
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



    // 老师进入频道
    private void onMasterJoin() {
        ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId, masterAccount);

    }


    // 老师离开频道
    private void onMasterLeave() {
        if (isDestroyed()) {
            return;
        }
        Toast.makeText(activity, "房主退出了房间", Toast.LENGTH_SHORT).show();
        activity.finish();
    }


    // 将被取消权限的成员从画布移除, 并将角色置为初始状态
    private void memberOff(String account) {
//        accountsNewPermission.remove(account);
        if (isMasterAccount(account)) {
            return;
        }
        if (!ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
            return;
        }
        ChatRoomMemberCache.getInstance().removePermissionMember(roomId, account);
    }


    public void closeStudentPermission(String account) {
        memberOff(account);
    }


    public void onKickOutSuccess(String account) {
        if (!ChatRoomMemberCache.getInstance().hasPermission(roomId, account)) {
            return;
        }
        closeStudentPermission(account);
        // 通知更新的有权限的成员列表
        MsgHelper.getInstance().sendCustomMsg(roomId, MeetingOptCommand.ALL_STATUS);
    }


    public void studentPermissionOn(String account) {
        // 更新本地权限缓存, 添加新的成员
        ChatRoomMemberCache.getInstance().savePermissionMemberById(roomId, account);
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
                memberOff(oldAccount);
            }
        }
        newAccounts.removeAll(oldAccounts);
        ChatRoomMemberCache.getInstance().savePermissionMemberByIds(roomId, newAccounts);
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
            case R.id.btn_close_student_share: {
                closeStudentShare();
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

    /*
    屏幕分享的回调
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        if(resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity,"你拒绝了录屏请求！",Toast.LENGTH_SHORT).show();
            return;
        }
        NERtcEx.getInstance().enableLocalVideo(false); //先停止视频
        NERtcEx.getInstance().startScreenCapture(NERtcConstants.ScreenProfile.HD720P,data, new MediaProjection.Callback() { //2、再创建录屏capturer
            @Override
            public void onStop() {
                super.onStop();
                Toast.makeText(activity,"录屏已停止",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 视频分享
     */
    private void showVideo() {
        if (isShareScreen) {
            NERtcEx.getInstance().stopScreenCapture();          //关闭屏幕分享

            NERtcEx.getInstance().enableLocalVideo(true);
            isShareScreen = false;
            updateControlUI();
        }
    }

    /**
     * 屏幕分享
     */
    private void shareScreen() {

        if (!isShareScreen) {
            // 如果之前没有人在屏幕共享，则开启屏幕共享

            tryStartScreenCapturer();
            isShareScreen = true;
            updateControlUI();
        }

    }


    //  关闭学生的屏幕共享
    private void closeStudentShare() {
        MsgHelper.getInstance().updateRoomInfo(ShareType.VIDEO, null, roomId);
    }


    // 设置自己的摄像头是否开启
    private void switchMuteVideo() {
        if (muteVideo) {
            videoMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_video_on_selector);
            NERtcEx.getInstance().muteLocalVideoStream(false);
            muteVideo = false;
        } else {
            videoMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_video_off_selector);
            NERtcEx.getInstance().muteLocalVideoStream(true);
            muteVideo = true;
        }
    }

    // 设置自己的录音是否开启
    private void switchMuteAudio() {
        if (muteAudio) {
            audioMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_audio_on_selector);
            NERtcEx.getInstance().muteLocalAudioStream(false);
            muteAudio = false;
        } else {
            audioMuteSwitchBtn.setBackgroundResource(R.drawable.chat_room_audio_off_selector);
            NERtcEx.getInstance().muteLocalAudioStream(true);
            muteAudio = true;
        }
    }


    /*
    请求屏幕共享权限
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void tryStartScreenCapturer() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getActivity().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, CAPTURE_PERMISSION_REQUEST_CODE);
    }




    /**
     * @param interactor 大屏即将展示的页面
     */
    private void setBigPreviewLayout(Interactor interactor) {

    }


    private boolean checkRoom(String roomID) {
        return TextUtils.isEmpty(roomId) || !roomId.equals(roomID);
    }


    public boolean isMasterAccount(String account) {
        return TextUtils.equals(account, masterAccount);
    }






}
