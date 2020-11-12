package com.yyj.stydyroom.study.helper;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomUpdateInfo;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.yyj.stydyroom.study.util.MeetingConstant;
import com.yyj.stydyroom.study.util.MeetingOptCommand;
import com.yyj.stydyroom.study.util.ShareType;
import com.yyj.stydyroom.study.util.custom.PermissionAttachment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2016/5/5.
 */
public class MsgHelper {

    public static MsgHelper getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 发送点对点自定义通知
     */
    public void sendP2PCustomNotification(String roomId, int command, String toAccount, List<String> accountList) {

        CustomNotification notification = new CustomNotification();
        notification.setSessionId(toAccount); // 指定接收者
        notification.setSessionType(SessionTypeEnum.P2P);
        CustomNotificationConfig config = new CustomNotificationConfig();
        config.enablePush = false; // 不推送
        notification.setConfig(config);
        notification.setSendToOnlineUserOnly(true); // 不支持离线
        JSONObject data = new JSONObject();
        data.put("command", command);
        data.put("room_id", roomId);
        data.put("uids", accountList);
        JSONObject json = new JSONObject();
        json.put("data", data);
        json.put("type", 10);
        notification.setContent(json.toString());

        NIMClient.getService(MsgService.class).sendCustomNotification(notification);
    }


    /**
     * 群发自定义消息
     */
    public void sendCustomMsg(String roomId, MeetingOptCommand command) {

        PermissionAttachment attachment = new PermissionAttachment(roomId,
                command,
                ChatRoomMemberCache.getInstance().getPermissionMembers(roomId));

        ChatRoomMessage message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, attachment);
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false);
    }


    public void updateRoomInfo(ShareType shareType, String shareAccount, String roomID) {
        updateRoomInfo(shareType, shareAccount, roomID, null);
    }

    /**
     * 主播更新房间扩展信息，影响大屏（白板、视频、老师/学生 共享）
     *
     * @param shareType    大屏显示的状态
     * @param shareAccount null 取消共享 (前提是shareType = ShareType.VIDEO)
     */
    public void updateRoomInfo(ShareType shareType, String shareAccount, String roomID, RequestCallback<Void> callback) {

        ChatRoomUpdateInfo chatRoomUpdateInfo = new ChatRoomUpdateInfo();
        Map<String, Object> param = new HashMap<>();
        param.put(MeetingConstant.SHOW_TYPE, shareType.getValue());

        if (!TextUtils.isEmpty(shareAccount)) {
            param.put(MeetingConstant.SHARE_ID, shareAccount);
        }

        chatRoomUpdateInfo.setExtension(param);
        NIMClient.getService(ChatRoomService.class).updateRoomInfo(roomID, chatRoomUpdateInfo, true, param).setCallback(callback);
    }

    private static class InstanceHolder {
        final static MsgHelper instance = new MsgHelper();
    }
}
