package com.yyj.stydyroom.study.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hzsunyj on 2019-12-11.
 */
public class WhiteBoardPic {

    public static final String QUALITY_HIGH = "HIGH";

    public static final String QUALITY_MEDIUM = "MEDIUM";

    public static final String QUALITY_LOW = "LOW";

    private int duration;

    private int height;

    private int width;

    private String type;

    private int size;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("duration", duration);// 时长
            jsonObject.put("height", height);// 高
            jsonObject.put("width", width);// 宽
            jsonObject.put("type", type);// 质量
            jsonObject.put("size", size);// size
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
