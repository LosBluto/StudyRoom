package com.yyj.stydyroom.study.fragment.tab;



import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.ui.tab.reminder.ReminderId;

/**
 * Created by hzxuwen on 2015/12/14.
 */
public enum ChatRoomBottomTab {

    CHAT_ROOM_MESSAGE(0, ReminderId.SESSION, MessageTabFragmentAbs.class, R.string.chat_room_message, R.layout.chat_room_message_tab),
    ONLINE_PEOPLE(1, ReminderId.CONTACT, OnlinePeopleTabFragmentAbs.class, R.string.chat_room_online_people, R.layout.chat_room_people_tab);

    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends AbsTabFragment> clazz;

    public final int resId;

    public final int layoutId;

    ChatRoomBottomTab(int index, int reminderId, Class<? extends AbsTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.layoutId = layoutId;
    }

    public static ChatRoomBottomTab fromTabIndex(int tabIndex) {
        for (ChatRoomBottomTab value : ChatRoomBottomTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }

    public static ChatRoomBottomTab fromReminderId(int reminderId) {
        for (ChatRoomBottomTab value : ChatRoomBottomTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }
}
