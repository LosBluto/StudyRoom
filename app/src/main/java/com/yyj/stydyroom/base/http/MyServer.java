package com.yyj.stydyroom.base.http;

import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.yyj.stydyroom.views.data.MyCache;


import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MyServer {
    private static final String TAG = "MY_SERVER";
    private Handler uiHandler = new Handler(MyCache.getContext().getMainLooper());

    private static MyServer instance;
    public interface MyCallBack<T>{
         void onSuccess(T t);

         void onFailed(int code,String errMsg);
    }

    public static synchronized MyServer getInstance(){
        if (instance == null)
            instance = new MyServer();
        return instance;
    }

    public MyServer() {
        NimHttpClient.getInstance().init(MyCache.getContext());
    }

    public void register(String account, String nickName, String password, final MyCallBack<String> callback) {
        String url = "http://47.112.137.62:8080/register";

        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account",account);
        jsonObject.put("password",password);
        jsonObject.put("name",nickName);

        NimHttpClient.getInstance().execute(url, headers, jsonObject.toString(), new NimHttpClient.NimHttpCallback() {
                    @Override
                    public void onResponse(String response, int code, String errorMsg) {
                        if (code != 0) {
                            Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + errorMsg);
                            if (callback != null) {
                                callback.onFailed(code, errorMsg);
                            }
                            return;
                        }

                        try {
                            JSONObject result = JSONObject.parseObject(response);

                            if (result.getString("code").equals("0")){
                                String message = result.getString("data");
                                callback.onSuccess(message);
                            }else {
                                Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + result.getString("message"));
                                callback.onFailed(-1,result.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFailed(-1,e.getMessage());
                        }
                    }
                }
        );
    }

    public void createRoom(int roomType,String account, String roomName , final MyCallBack<String> callback){
        Log.i("createRoom","starting createRoom");
        String url = "http://47.112.137.62:8080/createRoom";

        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account",account);
        jsonObject.put("name",roomName);
        jsonObject.put("room_type",String.valueOf(roomType));

        NimHttpClient.getInstance().execute(url, headers, jsonObject.toString(), new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject result = JSONObject.parseObject(response);
                    if (result.getString("code").equals("0")){
                        String message = result.getString("data");
                        JSONObject jsonObject1 = JSONObject.parseObject(message);
                        callback.onSuccess(jsonObject1.getString("roomid"));
                    }else {
                        Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + result.getString("message"));
                        callback.onFailed(-1,result.getString("message"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    callback.onFailed(-1,e.getMessage());
                }
            }
        });
    }

    public void closeRoom(String account,String roomId,final MyCallBack<String> callback){
        String url = "http://47.112.137.62:8080/closeRoom";

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("account",account);
        jsonObject.put("roomid",roomId);

        NimHttpClient.getInstance().execute(url, headers, jsonObject.toString(), new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject result =  JSONObject.parseObject(response);
                    if (result.getString("code").equals("0")) {
                        String message = result.getString("data");
                        callback.onSuccess(message);
                    }else {
                        Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + result.getString("message"));
                        callback.onFailed(-1, result.getString("message"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    callback.onFailed(-1,e.getMessage());
                }
            }
        });
    }

}

