package com.yyj.stydyroom.study.util;

import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yyj.stydyroom.base.http.MyServer;
import com.yyj.stydyroom.base.http.NimHttpClient;
import com.yyj.stydyroom.study.util.beans.ChatRoom;
import com.yyj.stydyroom.views.data.MyCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomServer {
    private static final String TAG = "ChatRoomServer";

    private static ChatRoomServer instance;
    public static Gson gson = new Gson();

    public ChatRoomServer(){
        NimHttpClient.getInstance().init(MyCache.getContext());
    }

    public static synchronized ChatRoomServer getInstance(){
        if (instance == null){
            instance = new ChatRoomServer();
        }
        return instance;
    }

    public interface ChatRoomCallBack<T>{
        void onSuccess(T data);

        void onFailed(int code,String message);
    }

    public void SelectChatRooms(final ChatRoomServer.ChatRoomCallBack<List<ChatRoom>> callback){
        String url = "http://47.112.137.62:8080/selectChatRooms";

        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        NimHttpClient.getInstance().execute(url, headers, null,false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    ErrCallBackRooms(code,errorMsg,callback);
                    return;
                }

                try {
                    JSONObject result = JSONObject.parseObject(response);
                    if (!result.getString("code").equals("0")){
                        ErrCallBackRooms(-1,result.getString("message"),callback);
                    }

                    String data = result.getString("data");
                    List<ChatRoom> rooms = gson.fromJson(data, new TypeToken<List<ChatRoom>>(){}.getType());
                    callback.onSuccess(rooms);
                }catch (JSONException e){
                    e.printStackTrace();
                    callback.onFailed(-1,e.getMessage());
                }
            }
        });
    }

    public void selectChatRoomByRoomId(int roomId,final ChatRoomServer.ChatRoomCallBack<ChatRoom> callback){
        String url = "http://47.112.137.62:8080/selectChatRoomByRoomId";
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        String data = "?"+"roomId="+roomId;

        NimHttpClient.getInstance().execute(url+data, headers, null,false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0){
                    ErrCallBackRoom(code,errorMsg,callback);
                    return;
                }

                try {
                    JSONObject result = JSONObject.parseObject(response);
                    if (!result.getString("code").equals("0")){
                        ErrCallBackRoom(-1,result.getString("message"),callback);
                        return;
                    }

                    ChatRoom room = gson.fromJson(result.getString("data"),ChatRoom.class);
                    callback.onSuccess(room);
                }catch (JSONException e){
                    e.printStackTrace();
                    callback.onFailed(-1,e.getMessage());
                }
            }
        });
    }

    public void selectRoomByRoomType(int roomType,final ChatRoomServer.ChatRoomCallBack<List<ChatRoom>> callback){
        String url = "http://47.112.137.62:8080/selectRoomByRoomType";
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        String data = "?"+"roomType="+roomType;

        NimHttpClient.getInstance().execute(url + data, headers, null, false,new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0){
                    ErrCallBackRooms(code,errorMsg,callback);
                    return;
                }

                try {
                    JSONObject result = JSONObject.parseObject(response);
                    if (!result.getString("code").equals("0")){
                        ErrCallBackRooms(-1,result.getString("message"),callback);
                        return;
                    }
                    List<ChatRoom> data = gson.fromJson(result.getString("data"),new TypeToken<List<ChatRoom>>(){}.getType());
                    callback.onSuccess(data);
                }catch (JSONException e){
                    e.printStackTrace();
                    callback.onFailed(-1,e.getMessage());
                }
            }
        });
    }

    private  void ErrCallBackRoom(int code,String message,ChatRoomCallBack<ChatRoom> callBack){
        Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + message);
        callBack.onFailed(-1,message);
    }

    private  void ErrCallBackRooms(int code,String message,ChatRoomCallBack<List<ChatRoom>> callBack){
        Log.e(TAG, "create room failed : code = " + code + ", errorMsg = " + message);
        callBack.onFailed(-1,message);
    }

}
