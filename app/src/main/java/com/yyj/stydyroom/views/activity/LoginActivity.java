package com.yyj.stydyroom.views.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.http.MyServer;
import com.yyj.stydyroom.base.utils.CheckSumBuilder;
import com.yyj.stydyroom.study.activity.StudyActivity;
import com.yyj.stydyroom.views.data.AuthPreferences;
import com.yyj.stydyroom.views.data.MyCache;
import com.yyj.stydyroom.views.ui.widget.ClearableEditTextWithIcon;

import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {
    private static final String KICK_OUT = "KICK_OUT";

    private View loginLayout;
    private View registerLayout;

    private ClearableEditTextWithIcon loginAccountEdit;
    private ClearableEditTextWithIcon loginPasswordEdit;

    private ClearableEditTextWithIcon registerAccountEdit;
    private ClearableEditTextWithIcon registerNickNameEdit;
    private ClearableEditTextWithIcon registerPasswordEdit;

    private Button loginBtn;
    private Button registerBtn;
    private TextView switchModeBtn;  // 注册/登录切换按钮


    private boolean registerMode = false;   //是否为注册
    private boolean isRegisterInit = false;

    private AbortableFuture<LoginInfo> loginRequest;        //登陆时使用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        parseLogin();
        parseRegister();
        parseIntent();                  //判断是否为踢出
    }

    public static void start(Context context){
        start(context,false);
    }

    public static void start(Context context,boolean kickOut){
        Intent intent = new Intent(context,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }

    private void parseIntent() {
        boolean isKickOut = getIntent().getBooleanExtra(KICK_OUT, false);
        if (isKickOut) {
            Toast.makeText(this,"您的账号已被踢出",Toast.LENGTH_LONG).show();
        }
    }

    /*
    初始化登陆
     */
    private void parseLogin(){
        loginLayout = findViewById(R.id.login_layout);
        loginBtn = findViewById(R.id.done);
        updateBtn(loginBtn,false);

        loginAccountEdit = (ClearableEditTextWithIcon) findViewById(R.id.edit_login_account);
        loginPasswordEdit = (ClearableEditTextWithIcon) findViewById(R.id.edit_login_password);

        loginAccountEdit.setIconResource(R.drawable.login);
        loginPasswordEdit.setIconResource(R.drawable.password);

        loginAccountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        loginPasswordEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        loginAccountEdit.addTextChangedListener(textWatcher);
        loginPasswordEdit.addTextChangedListener(textWatcher);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    private void login(){
        final String account = loginAccountEdit.getEditableText().toString().toLowerCase();
        final String token = tokenFromPassword(loginPasswordEdit.getEditableText().toString());
        Log.i("token:",token);

        loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(account,token)); //登陆
        loginRequest.setCallback(new RequestCallback<LoginInfo>() {                            //登陆的callback
            @Override
            public void onSuccess(LoginInfo param) {
                onLoginDone();
                MyCache.setAccount(account);                //缓存用户账户,下次登陆使用
                saveLoginInfo(account,token);


                Toast.makeText(LoginActivity.this,"登陆成功: "+ param.getAccount(),Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, StudyActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(int code) {
                onLoginDone();
                if (code == 302 || code == 404) {
                    Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable exception) {
                onLoginDone();
            }
        });

    }

    /*
    把密码先base64再md5
     */
    private String tokenFromPassword(String password){
        return CheckSumBuilder.getMD5(CheckSumBuilder.getBase64(password));
    }

    private void onLoginDone(){
        loginRequest = null;            //重置登陆信息
    }


    /*
    保存用户的信息
     */
    private void saveLoginInfo(String account,String token){
        AuthPreferences.saveUserAccount(account);
        AuthPreferences.saveUserToken(token);

    }

    /*
    文本框监听
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!registerMode){             //不在注册
                boolean isEnable = loginAccountEdit.getText().length()>0
                        && loginPasswordEdit.getText().length()>0 && isInteger(loginAccountEdit.getText().toString());
                updateBtn(loginBtn,isEnable);
            }
        }
    };

    /*
    改变按钮的状态
     */
    private void updateBtn(Button button,boolean isEnable){
        button.setEnabled(isEnable);

        button.setBackground(isEnable?getResources().getDrawable(R.drawable.actionbar_green_bg)
                :getResources().getDrawable(R.drawable.anctionbar_gray_bg));

    }

    /*
    初始化注册界面
     */
    private void parseRegister(){
        registerLayout = findViewById(R.id.register_layout);
        registerBtn = (Button) findViewById(R.id.register_btn);
        switchModeBtn = (TextView) findViewById(R.id.register_login_tip);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        switchModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode();
            }
        });
    }

    private void register() {
        if (registerMode && !isRegisterInit)
            return;
        if (!checkRegisterContentValid(true))
            return;



        // 注册流程
        final String account = registerAccountEdit.getText().toString();
        final String nickName = registerNickNameEdit.getText().toString();
        final String password = registerPasswordEdit.getText().toString();

        MyServer.getInstance().register(account, nickName, password, new MyServer.MyCallBack<String>() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(LoginActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                switchMode();                       // 切换回登录
                loginAccountEdit.setText(account);
                loginPasswordEdit.setText(password);

                registerAccountEdit.setText("");            //清空注册
                registerNickNameEdit.setText("");
                registerPasswordEdit.setText("");
            }

            @Override
            public void onFailed(int code, String errMsg) {
                Looper.prepare();
                Toast.makeText(LoginActivity.this, getString(R.string.register_failed, code, errMsg), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /*
    检测注册信息是否正确
     */
    private boolean checkRegisterContentValid(boolean tipError) {
        if (!registerMode || !isRegisterInit) {
            return false;
        }


        String account = registerAccountEdit.getText().toString();
        // 帐号检查
        if (registerAccountEdit.length() <= 0 || registerAccountEdit.length() > 20 || !isInteger(account)) {
            if (tipError) {
                Toast.makeText(this, R.string.register_account_tip, Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        // 昵称检查
        if (registerNickNameEdit.length() <= 0 || registerNickNameEdit.length() > 10
                || registerNickNameEdit.getText().toString().trim().isEmpty()) {
            if (tipError) {
                Toast.makeText(this, R.string.register_nick_name_tip, Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        // 密码检查
        if (registerPasswordEdit.length() < 6 || registerPasswordEdit.length() > 20) {
            if (tipError) {
                Toast.makeText(this, R.string.register_password_tip, Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        return true;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }


    /**
                                        改变界面
     */
    private void switchMode(){
        registerMode = !registerMode;           //改变模式
        if (registerMode && !isRegisterInit){       //初始化注册界面

            registerAccountEdit = (ClearableEditTextWithIcon) findViewById(R.id.edit_register_account);
            registerNickNameEdit = (ClearableEditTextWithIcon) findViewById(R.id.edit_register_nickname);
            registerPasswordEdit = (ClearableEditTextWithIcon) findViewById(R.id.edit_register_password);

            registerAccountEdit.setIconResource(R.drawable.login);
            registerNickNameEdit.setIconResource(R.drawable.name);
            registerPasswordEdit.setIconResource(R.drawable.password);

            registerAccountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            registerNickNameEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            registerPasswordEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

            registerAccountEdit.addTextChangedListener(textWatcher);
            registerNickNameEdit.addTextChangedListener(textWatcher);
            registerPasswordEdit.addTextChangedListener(textWatcher);

            isRegisterInit = true;
        }

        setTitle(registerMode?"注册":"登陆");
        loginLayout.setVisibility(registerMode?View.GONE:View.VISIBLE);
        registerLayout.setVisibility(registerMode?View.VISIBLE:View.GONE);
        switchModeBtn.setText(registerMode?"已有账号? 直接登陆":"注册");
        if (registerMode){
            updateBtn(registerBtn,true);
        }else {
            boolean isEnable = loginAccountEdit.getText().length() > 0
                    && loginPasswordEdit.getText().length() > 0;
            updateBtn(registerBtn, isEnable);
        }
    }
}