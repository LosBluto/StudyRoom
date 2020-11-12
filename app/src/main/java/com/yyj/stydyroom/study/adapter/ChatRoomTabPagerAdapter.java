package com.yyj.stydyroom.study.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;



import com.yyj.stydyroom.study.fragment.tab.AbsTabFragment;
import com.yyj.stydyroom.study.fragment.tab.ChatRoomBottomTab;
import com.yyj.stydyroom.study.fragment.tab.ChatRoomTopTab;
import com.yyj.stydyroom.study.ui.tab.SlidingTabPagerAdapter;
import com.yyj.stydyroom.study.util.NonScrollViewPager;

import java.util.List;

/**
 * Created by hzxuwen on 2015/12/14.
 */
public class ChatRoomTabPagerAdapter extends SlidingTabPagerAdapter {
    private int length;

    private int type;

    public interface TYPE {
        int BOTTOM = 0;
        int TOP = 1;
    }


    public ChatRoomTabPagerAdapter(FragmentManager fm, Context context, NonScrollViewPager pager, int length, int type) {
        super(fm, length, context.getApplicationContext(), pager);
        this.length = length;
        this.type = type;
        switch (type) {
            case TYPE.TOP:
                setTopDataList(fm, length);
                break;
            case TYPE.BOTTOM:
                setBottomDataList(fm, length);
                break;


        }

    }

    public void setTopDataList(FragmentManager fm, int length) {
        for (int i = 0; i < length; i++) {
            try {
                AbsTabFragment fragment = null;

                List<Fragment> fs = fm.getFragments();
                if (fs != null) {
                    for (Fragment f : fs) {
                        if (f.getClass() == ChatRoomTopTab.fromTabIndex(i).clazz) {
                            fragment = (AbsTabFragment) f;
                            break;
                        }
                    }
                }

                if (fragment == null) {
                    fragment = ChatRoomTopTab.fromTabIndex(i).clazz.newInstance();
                }

                fragment.setState(this);
                fragment.setInnerFragmentLayoutId(ChatRoomTopTab.fromTabIndex(i).layoutId);

                fragments[i] = fragment;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBottomDataList(FragmentManager fm, int length) {
        for (int i = 0; i < length; i++) {
            try {
                AbsTabFragment fragment = null;

                List<Fragment> fs = fm.getFragments();
                if (fs != null) {
                    for (Fragment f : fs) {
                        if (f.getClass() == ChatRoomBottomTab.fromTabIndex(i).clazz) {
                            fragment = (AbsTabFragment) f;
                            break;
                        }
                    }
                }

                if (fragment == null) {
                    fragment = ChatRoomBottomTab.fromTabIndex(i).clazz.newInstance();
                }

                fragment.setState(this);
                fragment.setInnerFragmentLayoutId(ChatRoomBottomTab.fromTabIndex(i).layoutId);
                fragments[i] = fragment;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public int getCacheCount() {
        return length;
    }

    @Override
    public int getCount() {
        return length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (type) {
            case TYPE.BOTTOM:
                ChatRoomBottomTab tabBottom = ChatRoomBottomTab.fromTabIndex(position);
                int resIdBottom = tabBottom != null ? tabBottom.resId : 0;
                return resIdBottom != 0 ? context.getText(resIdBottom) : "";
            case TYPE.TOP:
                ChatRoomTopTab tabTop = ChatRoomTopTab.fromTabIndex(position);
                int resIdTop = tabTop != null ? tabTop.resId : 0;
                return resIdTop != 0 ? context.getText(resIdTop) : "";
        }
        return "";


    }

    public void setLength(int length) {
        this.length = length;
    }
}
