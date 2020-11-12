package com.yyj.stydyroom.base.utils;

import android.app.Activity;
import android.content.Context;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.yyj.stydyroom.views.activity.LoginActivity;
import com.yyj.stydyroom.views.data.AuthPreferences;
import com.yyj.stydyroom.views.data.MyCache;

public class LogoutHelper {
    public static void logout(Context context, boolean kickOut) {
        AuthPreferences.saveUserToken("");

        MyCache.clear();

        NIMClient.getService(AuthService.class).logout();

        // 启动登录
        LoginActivity.start(context, kickOut);
        ((Activity) context).finish();
    }
}
