package com.yyj.stydyroom.views.data;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthPreferences {
    public static final String USER_ACCOUNT_KEY = "account";
    public static final String USER_TOKEN_KEY = "token";

    public static void saveUserAccount(String account){
        saveString(USER_ACCOUNT_KEY,account);
    }

    public static String getUserAccount() {
        return getString(USER_ACCOUNT_KEY);
    }

    public static void saveUserToken(String token) {
        saveString(USER_TOKEN_KEY, token);
    }

    public static String getUserToken() {
        return getString(USER_TOKEN_KEY);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }
    static SharedPreferences getSharedPreferences() {
        return MyCache.getContext().getSharedPreferences("StudyRoom", Context.MODE_PRIVATE);
    }

}
