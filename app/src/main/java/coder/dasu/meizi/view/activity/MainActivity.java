package coder.dasu.meizi.view.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import coder.dasu.meizi.AppConstant;
import coder.dasu.meizi.MeiziApp;
import coder.dasu.meizi.R;
import coder.dasu.meizi.data.bean.DayPublish;
import coder.dasu.meizi.net.GankApi;
import coder.dasu.meizi.net.GankRetrofit;
import coder.dasu.meizi.net.response.GankResponse;
import coder.dasu.meizi.utils.ListUtils;
import coder.dasu.meizi.utils.TimeUtils;
import coder.dasu.meizi.view.GankDataFragmentFactory;
import coder.dasu.meizi.view.GankDataFragmentFactory.FragmentKey;
import coder.dasu.meizi.view.adapter.GankPagerFragmentAdapter;
import coder.dasu.meizi.view.base.SwipeRefreshActivity;
import coder.dasu.meizi.view.fragment.GankDataFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends SwipeRefreshActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mLastUpdateDay;
    private GankPagerFragmentAdapter mFragmentAdapter;
    private List<GankDataFragment> mFragmentList;
    private List<String> mGankDayList;

    @InjectView(R.id.main_tab_layout)
    TabLayout mTabLayout;
    @InjectView(R.id.main_content_viewpager)
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.inject(this);

        initVariable();
        bindWidgets();
        loadServiceData();
    }

    @Override
    public int provideContentView() {
        return R.layout.activity_main;
    }

    public void initVariable() {
        mFragmentList = new ArrayList<>();
        GankDataFragmentFactory factory = new GankDataFragmentFactory();
        for (FragmentKey key: FragmentKey.values()) {
            mFragmentList.add(factory.newFragment(key.getValue()));
        }
        mFragmentAdapter = new GankPagerFragmentAdapter(getSupportFragmentManager(), mFragmentList);

        mLastUpdateDay = MeiziApp.getConfigSP().getString(AppConstant.GANK_DAY_LAST_UPDATE_TIME);

        mGankDayList = new ArrayList<>();
        QueryBuilder<DayPublish> qb = mDaoSession.getDayPublishDao().queryBuilder();
        for (DayPublish day:qb.list()) {
            mGankDayList.add(day.getDay());
        }

    }

    public void bindWidgets() {
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void loadServiceData() {
        long today = TimeUtils.string2Milliseconds(TimeUtils.getCurTimeString(TimeUtils.DAY_SDF), TimeUtils.DAY_SDF);
        long lastUpdateDay = TextUtils.isEmpty(mLastUpdateDay)
                ? 0
                : TimeUtils.string2Milliseconds(mLastUpdateDay, TimeUtils.DAY_SDF);
        if (today > lastUpdateDay) {
            loadGankDayData();
        }
    }

    private void loadGankDayData() {
        GankApi gankApi = GankRetrofit.getGankService();
        Call<GankResponse<String>> call = gankApi.getGankDay();
        call.enqueue(getLoadGankDayCallback());
    }

    public Callback<GankResponse<String>> getLoadGankDayCallback() {
        return new Callback<GankResponse<String>>() {
            @Override
            public void onResponse(Call<GankResponse<String>> call, Response<GankResponse<String>> response) {
                Log.d(TAG, "loadGankDayData()->onResponse(): " + response.body().toString());
                if (response.isSuccessful()) {
                    MeiziApp.getConfigSP().putString(AppConstant.GANK_DAY_LAST_UPDATE_TIME,
                            TimeUtils.getCurTimeString(TimeUtils.DAY_SDF));
                    List<String> strList = response.body().results;
                    strList.removeAll(mGankDayList);
                    List<DayPublish> dayList = new ArrayList<>();
                    for (String s:strList) {
                        DayPublish day = new DayPublish();
                        day.setDay(s);
                        dayList.add(day);
                    }
                    mDaoSession.getDayPublishDao().insertInTx(dayList);
                }
            }

            @Override
            public void onFailure(Call<GankResponse<String>> call, Throwable t) {
                Log.d(TAG, "loadGankDayData()->onFailure(): " + t.getMessage());
                Snackbar.make(mViewPager, "加载失败", Snackbar.LENGTH_INDEFINITE)
                        .setAction("重试", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadGankDayData();
                            }
                        })
                        .show();
            }
        };
    }

    @Override
    public void loadData() {
        if (!ListUtils.isEmpty(mFragmentList)) {
            mFragmentAdapter.getCurrentFragment().loadData();
        }
    }
}
