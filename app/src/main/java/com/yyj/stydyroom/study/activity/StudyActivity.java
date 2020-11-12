package com.yyj.stydyroom.study.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.yyj.stydyroom.R;
import com.yyj.stydyroom.base.utils.LogoutHelper;
import com.yyj.stydyroom.views.data.AuthPreferences;
import com.yyj.stydyroom.views.data.MyCache;

public class StudyActivity extends AppCompatActivity {

    View userInfoView;
    ImageView userHeadImage;
    TextView userNameText;
    TextView userAccountText;

    Button enterRoom;
    Button searchRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        parseUserInfo();
        parseView();
    }

    private void parseUserInfo(){
        userInfoView = findViewById(R.id.study_userInfo);
        userHeadImage = userInfoView.findViewById(R.id.image_head);
        userNameText  = userInfoView.findViewById(R.id.text_userName);
        userAccountText = userInfoView.findViewById(R.id.text_account);

        NimUserInfo userInfo = MyCache.getUserInfo();
        if (userInfo != null)
            AuthPreferences.saveUserName(userInfo.getName());

        userNameText.append(userInfo==null? MyCache.getName() : userInfo.getName());
        userAccountText.append(userInfo==null? MyCache.getAccount() : userInfo.getAccount());
    }

    private void parseView(){
        enterRoom = findViewById(R.id.study_button_enterRoom);
        searchRoom = findViewById(R.id.study_button_searchRoom);
        enterRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterRoomActivity.start(StudyActivity.this,true);
            }
        });
        searchRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterRoomActivity.start(StudyActivity.this,false);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_study, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                LogoutHelper.logout(StudyActivity.this, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}