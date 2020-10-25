package com.yyj.stydyroom.base.http;

import android.os.Handler;
import android.util.Log;

import com.yyj.stydyroom.views.data.MyCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyServer {
    private static OkHttpClient client = new OkHttpClient();
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

    public void register(String account, String nickName, String password,final MyCallBack<Void> callBack){
        String url = "http://47.112.137.62:8080/register";

        RequestBody requestBody = new FormBody.Builder()
                .add("account",account)
                .add("password",password)
                .add("name",nickName)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            int code = response.code();
            String resp = response.body().string();
            if (code != 200) {
                Log.e("register", "register failed : code = " + code + ", errorMsg = " + resp);
                if (callBack != null) {
                    callBack.onFailed(code, resp);
                }
                return;
            }

            try {
                JSONObject resObj = new JSONObject(resp);
                int resCode = resObj.getInt("code");
                if (resCode == 0) {             //让callback在ui线程中
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onSuccess(null);
                        }
                    });
                } else {
                    String error = resObj.getString("message");
                    callBack.onFailed(resCode, error);
                }
            } catch (JSONException e) {
                callBack.onFailed(-1, e.getMessage());
            }
        }
    });
    }

}