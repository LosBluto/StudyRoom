package com.yyj.stydyroom.study.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by hzsunyj on 2019-12-11.
 */
public class WhiteBoardReplayInfo {

    private String mode;

    private boolean debug;

    private String account;

    private String identity;

    private String ownerAccount;

    /**
     * 回放是否采用兼容模式， 录制文件如果是在旧版本的SDK上录制的，需要开启这个；新白板的录制不需要设置这个
     */
    private boolean isCompatible;

    private List<WhiteBoardReplayFile> fileList;

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public void setCompatible(boolean compatible) {
        isCompatible = compatible;
    }

    public void setFileList(List<WhiteBoardReplayFile> fileList) {
        this.fileList = fileList;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mode", mode);
            jsonObject.put("debug", debug);
            jsonObject.put("account", account);
            jsonObject.put("identity", identity);
            jsonObject.put("ownerAccount", ownerAccount);
            jsonObject.put("isCompatible", isCompatible);
            JSONArray files = new JSONArray();
            if (fileList != null && fileList.size() > 0) {
                for (WhiteBoardReplayFile file : fileList) {
                    files.put(file.toJson());
                }
            }
            jsonObject.put("files", files);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
