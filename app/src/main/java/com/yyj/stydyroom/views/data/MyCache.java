package com.yyj.stydyroom.views.data;

import android.content.Context;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

public class MyCache {
    private static Context context;

    private static String account;

    private static NimUserInfo userInfo;

    public static void clear() {
        account = null;
        userInfo = null;
    }

    public static String getAccount() {
        return account;
    }

    public static void setAccount(String account) {
        MyCache.account = account;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        MyCache.context = context.getApplicationContext();
    }

    public static NimUserInfo getUserInfo() {
        if (userInfo == null) {
            userInfo = NIMClient.getService(UserService.class).getUserInfo(account);
        }

        return userInfo;
    }
}
