package com.yyj.stydyroom.base.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;



public abstract class TFragment extends Fragment {

    private static final String TAG = "Fragment";
    protected View rootView;

    private final Handler handler = new Handler();

    private boolean destroyed = true;

    protected final boolean isDestroyed() {
        return destroyed;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG,"fragment: " + getClass().getSimpleName() + " onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        destroyed = false;
    }

    public void onDestroy() {
        destroyed = true;
        handler.removeCallbacks(null);
        super.onDestroy();
        Log.i(TAG,"fragment: " + getClass().getSimpleName() + " onDestroy()");
    }

    protected final Handler getHandler() {
        return handler;
    }

    protected final void postRunnable(final Runnable runnable) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                runnable.run();
            }
        });
    }

    protected final void postDelayed(final Runnable runnable, long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                runnable.run();
            }
        }, delay);
    }

    protected <T extends View> T findView(int resId) {
        if (rootView == null) {
            rootView = getView();
        }
        return (T) (rootView.findViewById(resId));
    }


}
