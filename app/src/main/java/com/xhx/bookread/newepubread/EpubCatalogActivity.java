package com.xhx.bookread.newepubread;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfsearch.R;
import com.xhx.bookread.activity.MBaseActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class EpubCatalogActivity extends MBaseActivity implements View.OnClickListener {

    private RecyclerView mListRv;

    // 通过 EventBus 初始化
    private ReadActivity mReadActivity;
    private List<EpubTocItem> mEpubTocItemList = new ArrayList<>();
    private OpfData mOpfData;
    private String mNovelUrl;
    private String mName;
    private String mCover;

    private List<String> mChapterNameList = new ArrayList<>();


    @Override
    public int getLayoutId() {

        return R.layout.activity_epub_catalog;
    }


    @Override
    public void initDatas() {
        EventBusUtil.register(this);
        for (int i = 0; i < mEpubTocItemList.size(); i++) {
            mChapterNameList.add(mEpubTocItemList.get(i).getTitle());
        }
        initView();
        doAfterInit();
    }

    protected void initView() {
        setTitle("目录");
        mListRv = findViewById(R.id.rv_epub_catalog_list);
        mListRv.setLayoutManager(new LinearLayoutManager(this));
        CatalogAdapter adapter = new CatalogAdapter(this, mChapterNameList);
        adapter.setOnCatalogListener(new CatalogAdapter.CatalogListener() {
            @Override
            public void clickItem(int position) {
                mReadActivity.finish();
                int chapterIndex = 0;
                // 计算阅读的章节
                for (int i = 0; i < mOpfData.getSpine().size(); i++) {
                    if (mEpubTocItemList.get(position).getPath()
                            .equals(mOpfData.getSpine().get(i))) {
                        chapterIndex = i;
                        break;
                    }
                }
                // 跳转活动
                Intent intent = new Intent(EpubCatalogActivity.this, ReadActivity.class);
                // 小说 url（本地小说为 filePath），参数类型为 String
                intent.putExtra(ReadActivity.KEY_NOVEL_URL, mNovelUrl);
                // 小说名，参数类型为 String
                intent.putExtra(ReadActivity.KEY_NAME, mName);
                // 小说封面 url，参数类型为 String
                intent.putExtra(ReadActivity.KEY_COVER, mCover);
                // 小说类型，0 为网络小说， 1 为本地 txt 小说，2 为本地 epub 小说
                // 参数类型为 int（非必需，不传的话默认为 0）
                intent.putExtra(ReadActivity.KEY_TYPE, 2);
                // 开始阅读的章节索引，参数类型为 int（非必需，不传的话默认为 0）
                intent.putExtra(ReadActivity.KEY_CHAPTER_INDEX, chapterIndex);
                startActivity(intent);
                finish();
            }
        });
        mListRv.setAdapter(adapter);
    }


    protected void doAfterInit() {
        StatusBarUtil.setLightColorStatusBar(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.catalog_bg));
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onStickyEventBusCome(Event event) {
        switch (event.getCode()) {
            case EventBusCode.EPUB_CATALOG_INIT:
                if (event.getData() instanceof EpubCatalogInitEvent) {
                    EpubCatalogInitEvent e = (EpubCatalogInitEvent) event.getData();
                    mReadActivity = e.getReadActivity();
                    mEpubTocItemList = e.getTocItemList();
                    mOpfData = e.getOpfData();
                    mNovelUrl = e.getNovelUrl();
                    mName = e.getNovelName();
                    mCover = e.getNovelCover();

                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtil.unregister(this);
    }
}
