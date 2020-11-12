package com.yyj.stydyroom.study.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.utils.LogoutHelper;
import com.yyj.stydyroom.study.ui.ClearableTextViewWithIcon;
import com.yyj.stydyroom.views.data.AuthPreferences;
import com.yyj.stydyroom.views.data.MyCache;

public class IdentifyAcitivity extends AppCompatActivity {

    View userInfoView;
    ImageView userHeadImage;
    TextView userNameText;
    TextView userAccountText;

    ClearableTextViewWithIcon FriendText;
    ClearableTextViewWithIcon SettingText;
    ClearableTextViewWithIcon LogoutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        registerObservers(true);
        parseUserInfo();
        parseIdentify();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    //检测用户登陆状态
    private void registerObservers(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (statusCode.wontAutoLogin()) {
                LogoutHelper.logout(IdentifyAcitivity.this, true);
            }
        }
    };

    /**
     * 初始化用户信息
     */
    private void parseUserInfo(){
        userInfoView = findViewById(R.id.userInfo);
        userHeadImage = userInfoView.findViewById(R.id.image_head);
        userNameText  = userInfoView.findViewById(R.id.text_userName);
        userAccountText = userInfoView.findViewById(R.id.text_account);


        NimUserInfo userInfo = MyCache.getUserInfo();
        if (userInfo != null)
            AuthPreferences.saveUserName(userInfo.getName());

        userNameText.append(userInfo==null? MyCache.getName() : userInfo.getName());
        userAccountText.append(userInfo==null? MyCache.getAccount() : userInfo.getAccount());
    }

    private void parseIdentify(){
        FriendText = findViewById(R.id.text_friend);
        SettingText = findViewById(R.id.text_setting);
        LogoutText = findViewById(R.id.text_logout);

        FriendText.setFirstIcon(R.drawable.friend);
        SettingText.setFirstIcon(R.drawable.setting);
        LogoutText.setFirstIcon(R.drawable.logout);

    }


}