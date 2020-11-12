package com.yyj.stydyroom.study.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class BaseWebView extends WebView {

    public BaseWebView(Context context) {
        super(context);
        init();
    }

    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface JsCallback {

        void onHandle(JSMessage message);
    }

    private JsApi jsApi;

    private JsCallback jsCallback;

    private List<OnScrollChangeListener> scrollChangeListeners = new LinkedList<>();

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            JSMessage message = (JSMessage) msg.obj;
            if (jsCallback != null) {
                jsCallback.onHandle(message);
            }
        }
    };

    public void setJsCallback(JsCallback jsCallback) {
        this.jsCallback = jsCallback;
    }

    @SuppressLint("JavascriptInterface")
    private void init() {
        jsApi = new JsApi(handler, this);
        addJavascriptInterface(jsApi, "jsBridge");
    }

    public void native2Web(String action, JSONObject params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", action);
            jsonObject.put("param", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("JSBridge", jsonObject.toString());
        String script = String.format("window.WebJSBridge(%s)", jsonObject.toString());
        callWeb(script);
    }

    private void callWeb(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script, null);
        } else {
            loadUrl("javascript:" + script);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (OnScrollChangeListener listener : scrollChangeListeners) {
            listener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    @MainThread
    public void addOnScrollChangeListener(OnScrollChangeListener listener) {
        scrollChangeListeners.remove(listener);
        scrollChangeListeners.add(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public interface OnScrollChangeListener {

        /**
         * Called when the scroll position of a view changes.
         *
         * @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }
}
