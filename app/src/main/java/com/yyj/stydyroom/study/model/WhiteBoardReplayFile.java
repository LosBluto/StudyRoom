package com.yyj.stydyroom.study.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hzsunyj on 2019-12-11.
 */
public class WhiteBoardReplayFile {

    private String account;

    private String url;

    private String uid;

    private String cid;

    private long timestamp;

    private int chunk;

    private boolean mixed;

    private String type;

    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * uid cid timestamp chunk mixed type 从url中提取
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public void setMixed(boolean mixed) {
        this.mixed = mixed;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account);
            jsonObject.put("url", url);
            jsonObject.put("uid", uid);
            jsonObject.put("cid", cid);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("chunk", chunk);
            jsonObject.put("mixed", mixed);
            jsonObject.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
