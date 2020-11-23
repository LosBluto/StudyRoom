package com.yyj.stydyroom.study.helper;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.constant.MemberQueryType;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yyj.stydyroom.study.util.MeetingOptCommand;
import com.yyj.stydyroom.study.util.custom.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.nimlib.sdk.msg.constant.NotificationType.ChatRoomInfoUpdated;

/**
 * 聊天室成员资料缓存
 * Created by huangjun on 2016/1/18.
 */
public class ChatRoomMemberCache {

    private static final String TAG = "ChatRoomMemberCache";

    public static ChatRoomMemberCache getInstance() {
        return InstanceHolder.instance;
    }

    //成员的缓存
    private Map<String, Map<String, ChatRoomMember>> cache = new HashMap<>();

    private Map<String, List<SimpleCallback<ChatRoomMember>>> frequencyLimitCache = new HashMap<>(); // 重复请求处理
    // 有音视频权限的人员缓存
    private Map<String, Map<String, ChatRoomMember>> permissionCache = new HashMap<>();

    //房间管理监听
    private List<MeetingControlObserver> meetingControlObservers = new ArrayList<>();

    //消息监听
    private List<RoomMsgObserver> roomMsgObservers = new ArrayList<>();
    //房间成员监听
    private List<RoomMemberChangedObserver> roomMemberChangedObservers = new ArrayList<>();
    //房间信息监听
    private List<RoomInfoChangedObserver> roomInfoChangedObservers = new ArrayList<>();




    private boolean isRTSOpen = false; // 白板是否启启用

    public void clear() {
        cache.clear();
        frequencyLimitCache.clear();
        roomMemberChangedObservers.clear();
        roomInfoChangedObservers.clear();
        permissionCache.clear();
        meetingControlObservers.clear();
        roomMsgObservers.clear();

    }

    public void clearRoomCache(String roomId) {
        if (cache.containsKey(roomId)) {
            cache.remove(roomId);
        }

        if (permissionCache.containsKey(roomId)) {
            permissionCache.remove(roomId);
        }

        setRTSOpen(false);
    }

    public boolean isRTSOpen() {
        return isRTSOpen;
    }

    public void setRTSOpen(boolean RTSOpen) {
        isRTSOpen = RTSOpen;
    }

    /************************ 群成员缓存 ****************************/

    /*
    双索引，第一个索引为roomid，第二个为account
     */
    public ChatRoomMember getChatRoomMember(String roomId, String account) {
        if (cache.containsKey(roomId)) {
            return cache.get(roomId).get(account);
        }

        return null;
    }

    public void saveMyMember(ChatRoomMember chatRoomMember) {
        saveMember(chatRoomMember);
    }

    /**
     * 从服务器获取聊天室成员资料（去重处理）（异步）（从服务器拉取当前房间中成员的信息）
     */
    private void fetchMember(final String roomId, final String account, final SimpleCallback<ChatRoomMember> callback) {
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(account)) {
            callback.onResult(false, null);
            return;
        }

        // 频率控制
        if (frequencyLimitCache.containsKey(account)) {
            if (callback != null) {
                frequencyLimitCache.get(account).add(callback);
            }
            return; // 已经在请求中，不要重复请求
        } else {//否则创建请求
            List<SimpleCallback<ChatRoomMember>> cbs = new ArrayList<>();
            if (callback != null) {
                cbs.add(callback);
            }
            frequencyLimitCache.put(account, cbs);
        }

        // fetch
        List<String> accounts = new ArrayList<>(1);
        accounts.add(account);
        NIMClient.getService(ChatRoomService.class).fetchRoomMembersByIds(roomId, accounts).setCallback(new RequestCallbackWrapper<List<ChatRoomMember>>() {
            @Override
            public void onResult(int code, List<ChatRoomMember> members, Throwable exception) {
                ChatRoomMember member = null;
                boolean hasCallback = !frequencyLimitCache.get(account).isEmpty();      //是否有请求
                boolean success = code == ResponseCode.RES_SUCCESS && members != null && !members.isEmpty();    //请求是否成功

                // cache
                if (success) {          //刷新成功
                    saveMembers(members);   //保存用户信息
                    member = members.get(0);
                } else {
                    Log.e(TAG, "fetch chat room member failed, code=" + code);
                }

                // callback
                if (hasCallback) {
                    List<SimpleCallback<ChatRoomMember>> cbs = frequencyLimitCache.get(account);
                    for (SimpleCallback<ChatRoomMember> cb : cbs) {             //响应该account用户的所有fetch请求
                        cb.onResult(success, member);
                    }
                }

                frequencyLimitCache.remove(account);                //请求处理完毕从队列中删除该请求
            }
        });
    }

    public void fetchRoomMembers(String roomId, MemberQueryType memberQueryType, long time, int limit,
                                 final SimpleCallback<List<ChatRoomMember>> callback) {
        if (TextUtils.isEmpty(roomId)) {
            callback.onResult(false, null);
            return;
        }

        NIMClient.getService(ChatRoomService.class).fetchRoomMembers(roomId, memberQueryType, time, limit).setCallback(new RequestCallbackWrapper<List<ChatRoomMember>>() {
            @Override
            public void onResult(int code, List<ChatRoomMember> result, Throwable exception) {
                boolean success = code == ResponseCode.RES_SUCCESS;

                if (success) {
                    saveMembers(result);
                } else {
                    Log.e(TAG, "fetch members by page failed, code:" + code);
                }

                if (callback != null) {
                    callback.onResult(success, result);
                }
            }
        });
    }

    private void saveMember(ChatRoomMember member) {
        //判断member信息是否正确，正确则从cache中取出房间
        if (member != null && !TextUtils.isEmpty(member.getRoomId()) && !TextUtils.isEmpty(member.getAccount())) {
            Map<String, ChatRoomMember> members = cache.get(member.getRoomId());
            //无此房间缓存
            if (members == null) {
                //新建缓存信息，并保存
                members = new HashMap<>();
                cache.put(member.getRoomId(), members);
            }

            members.put(member.getAccount(), member);
        }
    }

    private void saveMembers(List<ChatRoomMember> members) {
        if (members == null || members.isEmpty()) {
            return;
        }

        for (ChatRoomMember m : members) {
            saveMember(m);
        }
    }


    /**
     * 存储拥有音视频权限的成员列表
     */
    public void savePermissionMemberById(String roomId, String account) {
        if (TextUtils.isEmpty(account)) {
            return;
        }
        ChatRoomMember member = getChatRoomMember(roomId, account);
        if (member != null) {
            savePermissionMember(member);
        } else {
            fetchMember(roomId, account, (success, result) -> {
                if (success) {
                    savePermissionMember(result);
                }
            });
        }
    }

    public void savePermissionMemberByIds(String roomId, List<String> arrayList) {
        for (String account : arrayList) {
            savePermissionMemberById(roomId, account);
        }
    }

    /**
     * 单独存储有用音视频权限的成员
     */
    private void savePermissionMember(ChatRoomMember member) {
        //判断member是否合法
        if (member == null || TextUtils.isEmpty(member.getRoomId()) || TextUtils.isEmpty(member.getAccount())) {
            return;
        }
        //获取roomid下的成员map
        Map<String, ChatRoomMember> members = permissionCache.get(member.getRoomId());
        if (members == null) {  //map不为空重新创建
            members = new HashMap<>();
            permissionCache.put(member.getRoomId(), members);
        }

        members.put(member.getAccount(), member);
    }


    /**
     * 取消成员的音视频权限 (实际上就是把成员从权限成员缓存中移除)
     */
    public void removePermissionMember(String roomId, String account) {

        if (TextUtils.isEmpty(account)) {
            return;
        }

        Map<String, ChatRoomMember> map = permissionCache.get(roomId);
        if (map == null || map.isEmpty()) {
            return;
        }
        map.remove(account);
    }

    /**
     * 判断成员是否拥有音视频权限（判断是否在权限缓存中）
     */
    public boolean hasPermission(String roomId, String account) {
        if (TextUtils.isEmpty(account)) {
            return false;
        }

        Map<String, ChatRoomMember> map = permissionCache.get(roomId);
        if (map != null && !map.isEmpty() && map.containsKey(account)) {
            return true;
        }

        return false;
    }

    /**
     * 获取拥有音视频权限的成员帐号列表 (获取缓存的key set)
     */
    public List<String> getPermissionMembers(String roomId) {
        List<String> accounts = new ArrayList<>();
        Map<String, ChatRoomMember> map = permissionCache.get(roomId);

        if (map == null) {
            return null;
        }

        for (Object o : map.keySet()) {
            String key = o.toString();
            accounts.add(key);
        }
        return accounts;
    }





    private static class InstanceHolder {
        final static ChatRoomMemberCache instance = new ChatRoomMemberCache();
    }


    public void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(incomingChatRoomMsg, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotification, register);
    }

    /**
    聊天室消息的监听
     */
    private Observer<List<ChatRoomMessage>> incomingChatRoomMsg = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messages) {
            if (messages == null || messages.isEmpty()) {           //消息队列为空直接结束
                return;
            }

            for (IMMessage msg : messages) {            //依次处理消息
                if (msg == null) {
                    Log.e(TAG, "receive chat room message null");
                    continue;
                }

                Log.d(TAG, "receive msg type:" + msg.getMsgType());
                if (msg.getMsgType() == MsgTypeEnum.notification) {         //通知消息，调用通知
                    handleNotification(msg);
                }

                // 成员权限
                if (sendReceiveMemPermissions(msg)) {
                    return;
                }

                for (RoomMsgObserver observer : roomMsgObservers) {
                    observer.onMsgIncoming(messages);
                }
            }
        }
    };

    // 收到/发送成员权限缓存
    private boolean sendReceiveMemPermissions(IMMessage message) {

        if (message.getAttachment() == null ||
                !(message.getAttachment() instanceof PermissionAttachment)) {

            return false;
        }

        Log.d(TAG, "receive permission msg, return true");
        PermissionAttachment attachment = (PermissionAttachment) message.getAttachment();
        if (attachment.getMeetingOptCommand() == MeetingOptCommand.ALL_STATUS) {
            for (MeetingControlObserver observer : meetingControlObservers) {
                observer.onStatusNotify(attachment.getRoomId(), attachment.getAccounts());
            }
        } else if (attachment.getMeetingOptCommand() == MeetingOptCommand.GET_STATUS) {
            // 收到请求有权限的成员列表，如果自己有音视频权限，则发送消息告知对方
            for (MeetingControlObserver observer : meetingControlObservers) {
                observer.onSendMyPermission(attachment.getRoomId(), message.getFromAccount());
            }
        }
        return true;

    }

    /**
    处理通知消息
     */
    private void handleNotification(IMMessage message) {
        if (message.getAttachment() == null) {
            return;
        }

        String roomId = message.getSessionId();
        final ChatRoomNotificationAttachment attachment = (ChatRoomNotificationAttachment) message.getAttachment();
        if (attachment.getType() == ChatRoomInfoUpdated) {
            for (RoomInfoChangedObserver o : roomInfoChangedObservers) {
                o.onRoomInfoUpdate(message);
            }
        } else {
            fetchRoomMember(message, roomId, attachment);
        }
    }

    /**
    通知账户
     */
    private void fetchRoomMember(IMMessage message, String roomId, final ChatRoomNotificationAttachment attachment) {
        List<String> targets = attachment.getTargets();         //获取被通知的账户集合
        if (targets != null) {
            for (String target : targets) {
                ChatRoomMember member = getChatRoomMember(roomId, target);
                if (member != null) {
                    handleMemberChanged(attachment.getType(), member);
                } else {
                    fetchMember(roomId, message.getFromAccount(), new SimpleCallback<ChatRoomMember>() {
                        @Override
                        public void onResult(boolean success, ChatRoomMember result) {
                            if (success) {
                                handleMemberChanged(attachment.getType(), result);
                            }
                        }
                    });
                }

            }
        }
    }

    /**
    根据通知，处理指定成员状态
     */
    private void handleMemberChanged(NotificationType type, ChatRoomMember member) {
        if (member == null) {
            return;
        }
        switch (type) {
            case ChatRoomMemberIn:
                for (RoomMemberChangedObserver o : roomMemberChangedObservers) {
                    o.onRoomMemberIn(member);
                }
                break;
            case ChatRoomMemberExit:
                for (RoomMemberChangedObserver o : roomMemberChangedObservers) {
                    o.onRoomMemberExit(member);
                }
                break;
            case ChatRoomManagerAdd:
                member.setMemberType(MemberType.ADMIN);
                break;
            case ChatRoomManagerRemove:
                member.setMemberType(MemberType.NORMAL);
                break;
            case ChatRoomMemberBlackAdd:
                member.setInBlackList(true);
                break;
            case ChatRoomMemberBlackRemove:
                member.setInBlackList(false);
                break;
            case ChatRoomMemberMuteAdd:
                member.setMuted(true);
                break;
            case ChatRoomMemberMuteRemove:
                member.setMuted(false);
                member.setMemberType(MemberType.GUEST);
                break;
            case ChatRoomCommonAdd:
                member.setMemberType(MemberType.NORMAL);
                break;
            case ChatRoomCommonRemove:
                member.setMemberType(MemberType.GUEST);
                break;
            default:
                break;
        }

        saveMember(member);
    }

    /**
     * 自定义通知监听
     */
    private Observer<CustomNotification> customNotification = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            String content = customNotification.getContent();
            int command = 0;
            List<String> accounts = new ArrayList<>();
            String roomId = null;
            try {
                JSONObject json = JSON.parseObject(content);
                int id = json.getIntValue("type");
                if (id == 10) {
                    // 聊天室通知
                    JSONObject data = json.getJSONObject("data");
                    roomId = data.getString("room_id");
                    JSONArray array = data.getJSONArray("uids");
                    command = data.getIntValue("command");
                    for (int i = 0; i < array.size(); i++) {
                        accounts.add(array.get(i).toString());
                    }

                }
                Log.d(TAG, "receive custom notification, command:" + command);

            } catch (Exception e) {

            }
            if (command == MeetingOptCommand.SPEAK_ACCEPT.getValue()) {
                for (MeetingControlObserver observer : meetingControlObservers) {
                    observer.onAccept(roomId);
                }
            } else if (command == MeetingOptCommand.ALL_STATUS.getValue()) {
                // 老师刚入群，老师发所有成员权限给观众
                for (MeetingControlObserver observer : meetingControlObservers) {
                    observer.onSaveMemberPermission(roomId, accounts);
                }
            } else if (command == MeetingOptCommand.STATUS_RESPONSE.getValue()) {

                // 向所有人请求有权限的成员，有权限的成员返回的通知。
                for (MeetingControlObserver observer : meetingControlObservers) {
                    observer.onPermissionResponse(roomId, accounts);
                }
            } else if (command == MeetingOptCommand.SPEAK_REJECT.getValue()) {
                // 老师拒绝/挂断连麦
                for (MeetingControlObserver observer : meetingControlObservers) {
                    observer.onReject(roomId);
                }
            }  else if (command == MeetingOptCommand.SHARE_SCREEN.getValue()) {
                //有权限的成员发起屏幕分享通知
                for (MeetingControlObserver observer : meetingControlObservers) {
                    observer.onShareScreen(roomId, customNotification.getFromAccount());
                }
            } else if (command == MeetingOptCommand.CANCEL_SHARE_SCREEN.getValue()) {
                //取消屏幕共享
                for (MeetingControlObserver observer : meetingControlObservers) {
                    observer.onCancelShare(roomId, customNotification.getFromAccount());
                }
            }
        }
    };

    public interface MeetingControlObserver {

        /**
         * 老师同意举手的学生发言
         */
        void onAccept(String roomID);

        /**
         * 老师取消学生的发言权限
         */
        void onReject(String roomID);

        /**
         * 成员向所有人询问是否有互动权限时，有权限的成员返回的通知。
         */
        void onPermissionResponse(String roomId, List<String> accounts);


        /**
         * 学生刚入群，老师将有互动权限的列表通知给学生
         */
        void onSaveMemberPermission(String roomID, List<String> accounts);

        /**
         * 有其他学生发自定义消息询问是不是有互动权限
         */
        void onSendMyPermission(String roomID, String toAccount);


        /**
         * 老师将有互动权限的列表通知给所有人
         */
        void onStatusNotify(String roomID, List<String> accounts);


        /**
         * 学生申请屏幕共享
         */
        void onShareScreen(String roomID, String account);

        /**
         * 学生取消屏幕共享
         */
        void onCancelShare(String roomID, String account);

    }

    public void registerMeetingControlObserver(MeetingControlObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!meetingControlObservers.contains(o)) {
                meetingControlObservers.add(o);
            }
        } else {
            meetingControlObservers.remove(o);
        }
    }

    public interface RoomMsgObserver {
        void onMsgIncoming(List<ChatRoomMessage> messages);
    }


    public void registerRoomMsgObserver(RoomMsgObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!roomMsgObservers.contains(o)) {
                roomMsgObservers.add(o);
            }
        } else {
            roomMsgObservers.remove(o);
        }
    }

    public interface RoomMemberChangedObserver {
        void onRoomMemberIn(ChatRoomMember member);

        void onRoomMemberExit(ChatRoomMember member);
    }

    public void registerRoomMemberChangedObserver(RoomMemberChangedObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!roomMemberChangedObservers.contains(o)) {
                roomMemberChangedObservers.add(o);
            }
        } else {
            roomMemberChangedObservers.remove(o);
        }
    }


    public interface RoomInfoChangedObserver {
        void onRoomInfoUpdate(IMMessage message);
    }

    public void registerRoomInfoChangedObserver(RoomInfoChangedObserver o, boolean register) {
        if (o == null) {
            return;
        }

        if (register) {
            if (!roomInfoChangedObservers.contains(o)) {
                roomInfoChangedObservers.add(o);
            }
        } else {
            roomInfoChangedObservers.remove(o);
        }
    }
}
