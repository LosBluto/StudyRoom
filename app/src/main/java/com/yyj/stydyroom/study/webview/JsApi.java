package com.yyj.stydyroom.study.webview;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 */
public class JsApi {

    public static final int JS_CALL = 0x1;

    private Handler handler;

    private WebView webView;

    public JsApi(Handler handler, WebView webView) {
        this.handler = handler;
        this.webView = webView;
    }

    @Keep
    @JavascriptInterface
    public void nativeFunction(String params) {
        if (handler == null || TextUtils.isEmpty(params)) {
            return;
        }
        Message message = handler.obtainMessage(JS_CALL);
        try {
            JSONObject jsonObject = new JSONObject(params);
            String method = jsonObject.optString("action");
            String data = jsonObject.optString("param");
            message.obj = new JSMessage(method, data);
            message.sendToTarget();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
