package com.xhx.bookread.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.pdfsearch.R;
import com.orm.SugarRecord;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.xhx.bookread.adapter.FileFenLeiAdapter;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.configs.DatabaseConfig;
import com.xhx.bookread.util.PublicUtil;

import java.util.ArrayList;
import java.util.List;

public class AllActivity extends MBaseActivity {
    int action = 0;
    FileFenLeiAdapter fileFenLeiAdapter;
    List<FilesBean> filesBeans = new ArrayList<>();
    RefreshLayout refreshLayout;
    private final int pageSize = 10;
    private int page = 1;
    private String mKeyWords = "";
    EditText etSearch;

    public static void startAct(Context context, int action) {
        Intent intent = new Intent(context, AllActivity.class);
        intent.putExtra("action", action);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_all;
    }

    @Override
    public void initToolBar() {
        super.initToolBar();
        action = getIntent().getIntExtra("action", 0);
        if (action == 0) {
            finish();
            return;
        }
        switch (action) {
            case 1://查看全部
                setTitle("全部");
                break;
            case 2://搜索
                setTitle("搜索");
                break;
        }
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                loadMore(mKeyWords);
            }
        });
        RecyclerView rvSearch = findViewById(R.id.rvSearch);
        rvSearch.setLayoutManager(new GridLayoutManager(this, 2));
        fileFenLeiAdapter = new FileFenLeiAdapter(filesBeans, false);
        rvSearch.setAdapter(fileFenLeiAdapter);
        fileFenLeiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FilesBean filesBean = fileFenLeiAdapter.getItem(position);
                PublicUtil.openFile(AllActivity.this, filesBean);
            }
        });

        etSearch = findViewById(R.id.etSearch);
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    //先隐藏键盘
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    //其次再做相应操作

                }
                return false;
            }

        });

        findViewById(R.id.ivSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
    }

    private void search() {
        String etInput = etSearch.getText().toString();
        if (!TextUtils.isEmpty(etInput)) {
            mKeyWords = etSearch.getText().toString();
            page = 1;
            loadMore(mKeyWords);
        }
    }

    @Override
    public void initDatas() {
        switch (action) {
            case 1:
                loadMore("");
                break;
            case 2:

                break;
        }
    }

    private void loadMore(String keyWord) {
        if (page == 1) {
            filesBeans.clear();
        }
        int offset = (page - 1) * pageSize;
        int limit = pageSize;
        List<FilesBean> list;
        if (TextUtils.isEmpty(keyWord)) {
            list = SugarRecord.findWithQuery(FilesBean.class, DatabaseConfig.getQueryByLimitSql(DatabaseConfig.FILES_TAB_NAME, limit, offset));
        } else {
            list = SugarRecord.find(FilesBean.class, DatabaseConfig.getQueryByNameSql(), "%" + keyWord + "%", limit + "", offset + "");
        }
        filesBeans.addAll(list);
        if (!filesBeans.isEmpty()) {
            fileFenLeiAdapter.notifyDataSetChanged();
            if (filesBeans.size() < pageSize * page) {
                refreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                page++;
            }
        }
        refreshLayout.finishLoadMore();
    }


}
