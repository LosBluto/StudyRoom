package com.yyj.stydyroom.base.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.yyj.stydyroom.base.utils.ReflectionUtil;


public abstract class TActivity extends AppCompatActivity {
    private static final String TAG = "TActivity";


    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"activity: " + getClass().getSimpleName() + " onCreate()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"activity: " + getClass().getSimpleName() + " onDestroy()");
    }

    @Override
    public void onBackPressed() {
        invokeFragmentManagerNoteStateNotSaved();
        super.onBackPressed();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void invokeFragmentManagerNoteStateNotSaved() {
        ReflectionUtil.invokeMethod(getFragmentManager(), "noteStateNotSaved", null);
    }


    protected final Handler getHandler() {
        if (handler == null) {
            handler = new Handler(getMainLooper());
        }
        return handler;
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

}
