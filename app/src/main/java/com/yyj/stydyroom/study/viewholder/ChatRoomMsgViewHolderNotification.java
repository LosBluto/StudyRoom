package com.yyj.stydyroom.study.viewholder;

import android.widget.TextView;

import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.helper.ChatRoomNotificationHelper;
import com.yyj.stydyroom.study.session.viewholder.MsgViewHolderBase;


public class ChatRoomMsgViewHolderNotification extends MsgViewHolderBase {

    protected TextView notificationTextView;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_notification;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView = view.findViewById(R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        notificationTextView.setText(ChatRoomNotificationHelper.getNotificationText((ChatRoomNotificationAttachment) message.getAttachment()));
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}

