package com.yyj.stydyroom.study.fragment.tab;

import android.os.Bundle;

import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.fragment.ChatRoomMessageFragment;


/**
 * Created by hzxuwen on 2016/2/29.
 */
public class MessageTabFragmentAbs extends AbsTabFragment {

    private ChatRoomMessageFragment fragment;

    public MessageTabFragmentAbs() {
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    protected void onInit() {
        fragment = getInnerFragment(R.id.chat_room_message_fragment);
    }

    @Override
    public void onLeave() {
        if (fragment == null) {
            return;
        }
        fragment.onLeave();
    }

}
