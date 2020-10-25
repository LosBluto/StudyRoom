package com.yyj.stydyroom;

import android.app.Application;
import android.text.TextUtils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.yyj.stydyroom.views.data.AuthPreferences;
import com.yyj.stydyroom.views.data.MyCache;


public class MyApplication extends Application {

    public void onCreate() {
        //初始化
        super.onCreate();
        MyCache.setContext(this);

        NIMClient.init(this, loginInfo(), options());

    }

    /*
    获取用户信息
     */
    private LoginInfo loginInfo(){
        String account = AuthPreferences.getUserAccount();
        String token = AuthPreferences.getUserToken();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            MyCache.setAccount(account.toLowerCase());
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }

    /*
    sdk的配置
     */
    private SDKOptions options(){
        SDKOptions options = new SDKOptions();
//        options.appKey = getApplicationInfo().metaData.getString("com.netease.nim.appKey");
        return options;
    }
}
