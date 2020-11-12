package com.yyj.stydyroom.study.viewholder;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;


import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.utils.ScreenUtil;
import com.yyj.stydyroom.study.session.emoji.MoonUtil;
import com.yyj.stydyroom.study.session.viewholder.MsgViewHolderText;
import com.yyj.stydyroom.views.data.MyCache;

/**
 * Created by hzxuwen on 2016/1/18.
 */
public class ChatRoomViewHolderText extends MsgViewHolderText {
    @Override
    protected boolean isShowBubble() {
        return false;
    }

    @Override
    protected boolean isShowHeadImage() {
        return false;
    }

    @Override
    public void setNameTextView() {
        nameContainer.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
        ChatRoomViewHolderHelper.setNameTextView((ChatRoomMessage) message, nameTextView, nameIconView, context);
    }

    @Override
    protected void bindContentView() {
        TextView bodyTextView = findViewById(R.id.nim_message_item_text_body);
        bodyTextView.setTextColor(Color.BLACK);
        layoutDirection();
        MoonUtil.identifyFaceExpression(MyCache.getContext(), bodyTextView, getDisplayText(), ImageSpan.ALIGN_BOTTOM);
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());
        bodyTextView.setOnLongClickListener(longClickListener);
    }

    private void layoutDirection() {
        TextView bodyTextView = findViewById(R.id.nim_message_item_text_body);
        bodyTextView.setPadding(ScreenUtil.dip2px(6), 0, 0, 0);
    }
}
