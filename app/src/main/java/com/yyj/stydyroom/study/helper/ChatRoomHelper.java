package com.yyj.stydyroom.study.helper;



import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.yyj.stydyroom.views.data.MyCache;

/**
 * Created by hzxuwen on 2016/1/19.
 */
public class ChatRoomHelper {

    public static void init() {
        ChatRoomMemberCache.getInstance().clear();
        ChatRoomMemberCache.getInstance().registerObservers(true);
    }

    public static void logout() {
        ChatRoomMemberCache.getInstance().clear();
    }

    public static String showDisplayName(ChatRoomMessage message) {
        if (message.getChatRoomMessageExtension() != null) {
            return message.getChatRoomMessageExtension().getSenderNick();
        } else {
            return MyCache.getUserInfo() == null ? MyCache.getAccount() : MyCache.getUserInfo().getName();
        }
    }

}
