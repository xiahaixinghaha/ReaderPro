package com.xhx.bookread.activity;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.pdfsearch.R;
import com.google.android.material.tabs.TabLayout;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.FolderBean;
import com.xhx.bookread.fragment.FenLeiTextFragment;
import com.xhx.bookread.fragment.MBaseFragment;

import java.util.ArrayList;
import java.util.List;

public class FenLeiActivity extends MBaseActivity {
    List<MBaseFragment> fragments = new ArrayList<>();
    List<String> titles = new ArrayList<>();

    private static final String KEY_BEAN = "key_bean";
    public static final String KEY_ARG = "key_arg";
    public static final String KEY_INDEX = "key_index";
    int index=-1;

    public static void startFenLeiActivity(Context context, FolderBean folderBean) {
        Intent intent = new Intent(context, FenLeiActivity.class);
        intent.putExtra(KEY_BEAN, folderBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_fenlei;
    }


    private void initView() {
        FolderBean folderBean = (FolderBean) getIntent().getSerializableExtra(KEY_BEAN);
        if (folderBean == null) {
            index=-1;
        }else {
            index=folderBean.getFolderIndex();
        }
        initFragment();//初始化fragment列表
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);//绑定viewpager
//        for (element in titles) tabLayout.addTab(tabLayout.newTab().setText(element))
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @Override
            public int getCount() {
                return fragments.size();
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View view = getLayoutInflater().inflate(R.layout.layout_tab_main, null);
            tabLayout.getTabAt(i).setCustomView(view);
            TextView tvTabTitle = view.findViewById(R.id.tvTabTitle);
            tvTabTitle.setText(titles.get(i));
        }
        findViewById(R.id.ivSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllActivity.startAct(FenLeiActivity.this,2);
            }
        });
    }

    private void initFragment() {
        fragments.clear();
        titles.clear();

        FenLeiTextFragment fragmentText = new FenLeiTextFragment();
        addFragment(fragmentText, "图书", 1,index);

        FenLeiTextFragment fragmentVoice = new FenLeiTextFragment();
        addFragment(fragmentVoice, "有声书", 2,-1);

        FenLeiTextFragment fragmentVideo = new FenLeiTextFragment();
        addFragment(fragmentVideo, "视频", 3,-1);
    }

    private void addFragment(MBaseFragment fragment, String title, int kind,int index) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ARG, kind);
        bundle.putInt(KEY_INDEX, index);
        fragment.setArguments(bundle);
        titles.add(title);
        fragments.add(fragment);
    }

}
