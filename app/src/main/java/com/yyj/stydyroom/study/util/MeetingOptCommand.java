package com.yyj.stydyroom.study.util;

/**
 * Created by hzxuwen on 2016/4/25.
 */
public enum MeetingOptCommand {
    /**
     * 未知
     */
    NONE(-1),

    /**
     * 老师将权限的成员列表通知给所有人
     */
    ALL_STATUS(1),

    /**
     * 成员向所有人请求有权限的成员
     */
    GET_STATUS(2),

    /**
     * 有权限的成员向请求者返回自己有权限的通知
     */
    STATUS_RESPONSE(3),

    /**
     * 向老师请求连麦权限
     */
    SPEAK_REQUEST(10),

    /**
     * 老师同意连麦请求
     */
    SPEAK_ACCEPT(11),

    /**
     * 老师拒绝或关闭连麦
     */
    SPEAK_REJECT(12),

    /**
     * 取消向老师请求连麦权限
     */
    SPEAK_REQUEST_CANCEL(13),

    /**
     * 有权限的成员发起屏幕分享通知
     */
    SHARE_SCREEN(14),

    /**
     * 取消共享
     */
    CANCEL_SHARE_SCREEN(15);


    private int value;

    MeetingOptCommand(int value) {
        this.value = value;
    }

    public static MeetingOptCommand statusOfValue(int status) {
        for (MeetingOptCommand e : values()) {
            if (e.getValue() == status) {
                return e;
            }
        }
        return NONE;
    }

    public int getValue() {
        return value;
    }
}
