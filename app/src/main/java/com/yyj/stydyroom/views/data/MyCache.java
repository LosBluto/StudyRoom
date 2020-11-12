package com.yyj.stydyroom.views.data;

import android.content.Context;
import android.util.Log;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

public class MyCache {
    private static Context context;

    private static String account;

    private static String name;

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

    public static void setName(String name) {
        MyCache.name = name;
    }

    public static String getName() {
        return name;
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
            Log.i("MYCACHE",account);
        }

        return userInfo;
    }
}
