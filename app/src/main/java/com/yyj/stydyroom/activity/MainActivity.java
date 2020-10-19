package com.yyj.stydyroom.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.yyj.stydyroom.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化
        NIMClient.init(getApplicationContext(),loginInfo(),options());
    }

    /*
    获取用户信息
     */
    private LoginInfo loginInfo(){
        return null;
    }

    /*
    sdk的配置
     */
    private SDKOptions options(){
        SDKOptions options = new SDKOptions();
        options.appKey = getApplicationInfo().metaData.getString("com.netease.nim.appKey");
        return options;
    }
}