package com.yyj.stydyroom.study.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.video.AVChatTextureViewRenderer;
import com.yyj.stydyroom.base.http.MyServer;
import com.yyj.stydyroom.views.data.MyCache;

public class Interactor {

    private static final String TAG = "Interactor";

    public interface Status {

        int CAMERA = 0;//照相机界面

        int SHARE_SCREEN = 1;//屏幕共享
    }

    private final String account;

    private AVChatTextureViewRenderer renderer;//画布

    private int capturerType;//当前捕捉器模式

    private Context context;


    public Interactor(String account, Context context, int state) {
        this.account = account;
        this.renderer = new AVChatTextureViewRenderer(context);
        this.capturerType = state;
        this.context = context;

    }

    public String getAccount() {
        return account;
    }


    public AVChatTextureViewRenderer getRenderer() {
        return renderer;
    }


    public int getCapturerType() {
        return capturerType;
    }

    public void setCapturerType(int capturerType) {
        this.capturerType = capturerType;
    }


    public void release() {
        if (renderer.getParent() == null) {
            return;
        }
        removeFromParent();
        try {
            if (TextUtils.equals(account, MyCache.getAccount())) {
                AVChatManager.getInstance().setupLocalVideoRender(null, false,
                                                                  AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            } else {
                AVChatManager.getInstance().setupRemoteVideoRender(account, null, false,
                                                                   AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            }
        } catch (Throwable throwable) {
            renderer.release();
            Log.e(TAG, "release render err : " + throwable.getMessage());
            throwable.printStackTrace();
        }
        this.renderer = new AVChatTextureViewRenderer(context);
    }


    public void removeFromParent() {
        if (renderer.getParent() == null) {
            return;
        }
        ((ViewGroup) renderer.getParent()).removeAllViews();
    }

}
