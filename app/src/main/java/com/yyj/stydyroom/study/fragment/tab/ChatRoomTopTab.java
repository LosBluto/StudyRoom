package com.yyj.stydyroom.study.fragment.tab;


import com.yyj.stydyroom.R;

/**
 * Created by hzxuwen on 2015/12/14.
 */
public enum ChatRoomTopTab {

    RTS(1, RTSTabFragmentAbs.class, R.string.chat_room_rts, R.layout.chat_room_rts_tab),
    SCREEN_SHARE(0, ShareScreenTabFragmentAbs.class, R.string.chat_room_screen_share, R.layout.chat_room_share_screen_tab);

    public final int tabIndex;

    public final Class<? extends AbsTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    ChatRoomTopTab(int index, Class<? extends AbsTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static ChatRoomTopTab fromTabIndex(int tabIndex) {
        for (ChatRoomTopTab value : ChatRoomTopTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }
        return null;
    }


}
