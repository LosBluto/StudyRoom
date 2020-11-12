package com.yyj.stydyroom.study.fragment.tab;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.fragment.ShareScreenFragment;


/**
 * Created by hzxuwen on 2016/10/27.
 */

public class ShareScreenTabFragmentAbs extends AbsTabFragment {

    private ShareScreenFragment fragment;
    private View pendingView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    protected void onInit() {
        fragment = getInnerFragment(R.id.chat_room_share_screen_fragment);
        if (pendingView != null) {
            Log.i("AddVideoView", "onInit addVideoView : " + pendingView);
            fragment.addVideoView(pendingView);
            pendingView = null;
        }
    }

    public void addVideoView(View view) {
        Log.i("AddVideoView", "ShareScreenTabFragmentAbs addVideoView : " + view);
        if (fragment == null) {
            pendingView = view;
            return;
        }
        fragment.addVideoView(view);
    }


}
