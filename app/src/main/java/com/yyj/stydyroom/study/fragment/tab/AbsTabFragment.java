package com.yyj.stydyroom.study.fragment.tab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yyj.stydyroom.R;
import com.yyj.stydyroom.study.ui.tab.TabFragment;


/**
 * Created by hzxuwen on 2015/12/14.
 */
public abstract class AbsTabFragment extends TabFragment {

    private boolean loaded = false;
    private int innerFragmentLayoutId;

    protected abstract void onInit();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_fragment_container, container, false);
    }

    public void setInnerFragmentLayoutId(int layoutId) {
        innerFragmentLayoutId = layoutId;
    }

    @Override
    public void onCurrent() {
        if (loaded) {
            return;
        }
        if (loadRealLayout()) {
            loaded = true;
            onInit();
        }
    }

    protected boolean loadRealLayout() {
        ViewGroup root = (ViewGroup) getView();
        if (root != null) {
            root.removeAllViewsInLayout();
            View.inflate(root.getContext(), innerFragmentLayoutId, root);
        }
        return root != null;
    }


    protected <T extends Fragment> T getInnerFragment(int fragmentId) {
        return (T) getActivity().getSupportFragmentManager().findFragmentById(fragmentId);
    }
}
