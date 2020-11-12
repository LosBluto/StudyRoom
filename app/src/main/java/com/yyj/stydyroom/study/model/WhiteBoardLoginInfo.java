package com.yyj.stydyroom.study.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hzsunyj on 2019-12-11.
 */
public class WhiteBoardLoginInfo {

    private String mode;

    private String channelName;

    private boolean debug;

    private String appKey;

    private String account;

    private String token;

    private boolean record;

    private String identity;

    private String ownerAccount;

    private JSONObject custom;

    private JSONObject player;

    private WhiteBoardTools tools;

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }

    /**
     * 登录账号身份：白板房东'owner'去创建房间，如果房间存在则加入房间，其他成员'normal'加入房间。 两种身份初默认都是不可以绘画，
     * 需要调用 action=`enableDraw`开启绘画权限。
     *
     * @param identity
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public void setCustom(JSONObject custom) {
        this.custom = custom;
    }

    public void setPlayer(JSONObject player) {
        this.player = player;
    }

    public void setTools(WhiteBoardTools tools) {
        this.tools = tools;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mode", mode);// 互动模式 'interaction' ； 回放模式 为 'player'
            jsonObject.put("channelName", channelName);
            jsonObject.put("debug", debug);
            jsonObject.put("appKey", appKey);
            jsonObject.put("account", account);
            jsonObject.put("token", token);
            jsonObject.put("record", record);
            jsonObject.put("identity", identity);
            jsonObject.put("ownerAccount", ownerAccount);
            jsonObject.put("custom", custom);
            jsonObject.put("player", player);
            jsonObject.put("tools", tools.toJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
