package coder.dasu.meizi.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import coder.dasu.meizi.R;

/**
 * Created by dasu on 2016/9/26.
 * https://github.com/woshidasusu/Meizi
 */
public class ExpandDataFragment extends GankDataFragment {

    private static final String TAG = ExpandDataFragment.class.getSimpleName();

    public ExpandDataFragment(String type) {
        mType = type;
    }

    @Override
    public String getType() {
        return mType;
    }

    @Override
    public String getTAG() {
        return TAG;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_expand, container, false);
            ButterKnife.inject(this, rootView);
            bindWidgets();
        }
        return rootView;
    }

    private void bindWidgets() {

    }
}
