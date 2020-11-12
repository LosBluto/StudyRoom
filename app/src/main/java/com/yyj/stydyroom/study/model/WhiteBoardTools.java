package com.yyj.stydyroom.study.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hzsunyj on 2019-12-11.
 */
public class WhiteBoardTools {

    /**
     * 是否手动调用清除，如果设置为true，可以在customEvent里收到用户点击了清除
     */
    private boolean customClear;

    /**
     * 工具条展示条目，注意加入顺序
     */
    private JSONArray toolbar;


    public void setCustomClear(boolean customClear) {
        this.customClear = customClear;
    }

    public void setToolbar(JSONArray toolbar) {
        this.toolbar = toolbar;
    }

    //tools: {
    //    default:{color:'#000'},
    //    toolbar: [
    //    'flag', 'free', 'text', { type: 'shapes', items: ['line', 'rect', 'circle'] },
    //    'undo', 'clear',{ type: 'customTXT', label: '文档库', eventName: 'selectDoc' }
    //    ]
    //}
    public JSONObject toJson() {
        JSONObject tools = new JSONObject();
        try {
            tools.put("customClear", customClear);
            tools.put("toolbar", toolbar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tools;
    }
}
