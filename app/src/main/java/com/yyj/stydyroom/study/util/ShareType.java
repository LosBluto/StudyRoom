package com.yyj.stydyroom.study.util;

/**
 * Created by hzxuwen on 2016/12/12.
 */

public enum ShareType {

    /**
     * 视频
     */
    VIDEO(0),

    /**
     * 白板
     */
    WHITE_BOARD(1);

    private int value;

    ShareType(int value) {
        this.value = value;
    }

    public static ShareType statusOfValue(int status) {
        for (ShareType e : values()) {
            if (e.getValue() == status) {
                return e;
            }
        }
        return VIDEO;
    }

    public int getValue() {
        return value;
    }
}
