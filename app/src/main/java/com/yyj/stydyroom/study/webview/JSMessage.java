package com.yyj.stydyroom.study.webview;

import android.text.TextUtils;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

/**
 */
public class JSMessage {

    public static final String TAG = "JSMessage";

    private String method;

    /**
     * json
     */
    private String data;

    private String callbackId;

    public JSMessage(String method, String data) {
        this.method = method;
        this.data = data;
    }

    public JSMessage(String method, String data, String callbackId) {
        this.method = method;
        this.data = data;
        this.callbackId = callbackId;
    }

    public String getMethod() {
        return method;
    }

    public JSONObject getData() {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        try {
            return new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "js message data error");
        } catch (Exception e) {
            Log.e(TAG, "js message data error");
        }
        return null;
    }

    public String getCallbackId() {
        return callbackId;
    }
}
