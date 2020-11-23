package com.yyj.stydyroom;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MsgService;
import com.yyj.stydyroom.base.utils.ScreenUtil;
import com.yyj.stydyroom.base.utils.sys.SystemUtil;
import com.yyj.stydyroom.base.utils.util.storage.StorageUtil;
import com.yyj.stydyroom.inject.FlavorDependent;
import com.yyj.stydyroom.study.helper.ChatRoomHelper;
import com.yyj.stydyroom.views.activity.MainActivity;
import com.yyj.stydyroom.views.data.AuthPreferences;
import com.yyj.stydyroom.views.data.MyCache;
import com.yyj.stydyroom.views.data.UserPreferences;

import java.io.File;


public class MyApplication extends Application {
    private static final String TAG = "APPLICATION";

    public void onCreate() {
        //初始化
        super.onCreate();
        MyCache.setContext(this);

//        StorageUtil.init(this, ensureLogDirectory());
//        ScreenUtil.init(this);

        NIMClient.init(this, loginInfo(), options());

        if (inMainProcess()) {
            // 注册自定义消息附件解析器
            NIMClient.getService(MsgService.class).registerCustomAttachmentParser(FlavorDependent.getInstance().getMsgAttachmentParser());

            // init tools
//            StorageUtil.init(this, ensureLogDirectory());
//            ScreenUtil.init(this);


            FlavorDependent.getInstance().onApplicationCreate();
        }

    }

    private boolean inMainProcess() {
        String packageName = getPackageName();
        String processName = SystemUtil.getProcessName(this);
        return packageName.equals(processName);
    }

    /*
    获取用户信息
     */
    private LoginInfo loginInfo(){
        String account = AuthPreferences.getUserAccount();
        String token = AuthPreferences.getUserToken();
        String name = AuthPreferences.getUserName();

        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            Log.i(TAG,"already login"+account+" "+token);
            MyCache.setAccount(account.toLowerCase());
            MyCache.setName(name);
            return new LoginInfo(account, token);
        } else {
            Log.i(TAG,"not login");
            return null;
        }
    }

    /*
    sdk的配置
     */
    private SDKOptions options(){
        SDKOptions options = new SDKOptions();

//        // 如果将新消息通知提醒托管给SDK完成，需要添加以下配置。
//        StatusBarNotificationConfig config = UserPreferences.getStatusConfig();
//        if (config == null) {
//            config = new StatusBarNotificationConfig();
//        }
//        // 点击通知需要跳转到的界面
//        config.notificationEntrance = MainActivity.class;
//        config.notificationSmallIconId = R.drawable.ic_stat_notify_msg;
//
//        // 通知铃声的uri字符串
//        config.notificationSound = "android.resource://com.netease.nim.demo/raw/msg";
//        options.statusBarNotificationConfig = config;
//        UserPreferences.setStatusConfig(config);
//
//        // 配置保存图片，文件，log等数据的目录
//        String sdkPath = ensureLogDirectory();
//        options.sdkStorageRootPath = sdkPath;
//        Log.i("demo", " demo nim sdk log path=" + sdkPath);
//
//        // 配置数据库加密秘钥
//        options.databaseEncryptKey = "NETEASE";
//
//        // 配置是否需要预下载附件缩略图
//        options.preloadAttach = true;
//
//        // 配置附件缩略图的尺寸大小，
//        options.thumbnailSize = (int) (0.5 * ScreenUtil.screenWidth);
//
//        // 用户信息提供者
//        options.userInfoProvider = null;
//
//        // 定制通知栏提醒文案（可选，如果不定制将采用SDK默认文案）
//        options.messageNotifierCustomization = null;


        return options;
    }

    private void initNERtc(){
        try {
            NERtcEx.getInstance().init(getApplicationContext(),"f3be95142ec02f4683e11fc0c337e1ee",null,null);
        } catch (Exception e) {
            Toast.makeText(this,"SDK初始化失败",Toast.LENGTH_SHORT).show();
        }
    }

    private String ensureLogDirectory() {
        File log = getExternalFilesDir("nim");
        if (log == null) {
            log = getDir("nim", Context.MODE_PRIVATE);
        }
        return log.getAbsolutePath();
    }
}
