package com.yyj.stydyroom.study.helper;


import com.netease.nimlib.sdk.avchat.model.AVChatData;

import java.util.Map;

/**
 * Created by hzxuwen on 2016/4/25.
 */
public interface VideoListener {

    void onAVChatData(AVChatData data);

    void onVideoOff(String account);

    void onVideoOn(String account);

    void onTabChange(boolean notify);

    void onKickOutSuccess(String account);

    void onReportSpeaker(Map<String, Integer> map);

    /**
     * 老师同意观众申请发言，观众选择后确认
     */
    void onAcceptConfirm();

    /**
     * 老师取消学生的发言权限
     */
    void onReject();

    void onLineFragNotify();

    void onStatusNotify();

}
