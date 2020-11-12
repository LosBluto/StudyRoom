package com.yyj.stydyroom.study.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by hzsunyj on 2019-12-11.
 */
public class WhiteBoardDoc {

    private String docId;

    private String docName;

    private String urlPrefix;

    private int docSize;

    private String docType;

    private String transStat;

    private String transType;

    private int transSize;

    private int pageNum;

    private List<WhiteBoardPic> picList;


    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public void setDocSize(int docSize) {
        this.docSize = docSize;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public void setTransStat(String transStat) {
        this.transStat = transStat;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public void setTransSize(int transSize) {
        this.transSize = transSize;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public void setPicList(List<WhiteBoardPic> picList) {
        this.picList = picList;
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();
        try {
            data.put("docId", docId);//获取文档ID，文档ID是文档的唯一标识
            data.put("docName", docName);//doc name
            data.put("urlPrefix", urlPrefix);//获取文档下载链接前缀
            data.put("docSize", docSize);// h5 / img
            data.put("docType", docType);// h5 / img/ pptx
            data.put("transStat", transStat);//转码状态
            data.put("transType", transType);// 转码类型
            data.put("transSize", transSize);//转码尺寸
            data.put("pageNum", pageNum);// 页数
            JSONArray picInfo = new JSONArray();
            if (picList != null && picList.size() > 0) {
                for (WhiteBoardPic pic : picList) {
                    picInfo.put(pic.toJson());
                }
            }
            data.put("picInfo", picInfo);// 页数
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
}
