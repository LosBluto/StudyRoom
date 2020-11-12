package com.yyj.stydyroom.study.fragment.tab;


import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.fragment.OnlinePeopleFragment;

/**
 * Created by hzxuwen on 2016/2/29.
 */
public class OnlinePeopleTabFragmentAbs extends AbsTabFragment {

    private OnlinePeopleFragment fragment;

    @Override
    protected void onInit() {
        fragment = getInnerFragment(R.id.online_people_fragment);
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
        if (fragment == null) {
            return;
        }
        fragment.onCurrent();
    }
}
