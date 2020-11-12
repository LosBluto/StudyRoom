package com.yyj.stydyroom.study.fragment.tab;

import android.os.Bundle;

import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.fragment.ChatRoomRTSFragment2;


/**
 * Created by hzxuwen on 2016/10/27.
 */

public class RTSTabFragmentAbs extends AbsTabFragment {

    private ChatRoomRTSFragment2 fragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    protected void onInit() {
        fragment = getInnerFragment(R.id.chat_room_rts_fragment);
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
        if (fragment == null) {
            return;
        }
        fragment.onCurrent();
    }

}
