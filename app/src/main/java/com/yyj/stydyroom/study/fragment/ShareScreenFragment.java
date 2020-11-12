package com.yyj.stydyroom.study.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.ui.TFragment;


public class ShareScreenFragment extends TFragment {

    private ViewGroup videoLayout; //视频显示区域

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        videoLayout = (ViewGroup) inflater.inflate(R.layout.share_screen_fragment, container, false);
        return videoLayout;
    }


    public void addVideoView(View view) {
        videoLayout.removeAllViews();
        videoLayout.addView(view);
        Log.i("AddVideoView", "ShareScreenFragment addVideoView : " + view);
    }

}
