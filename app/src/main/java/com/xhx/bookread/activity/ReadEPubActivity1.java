package com.xhx.bookread.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;

import com.example.pdfsearch.R;
import com.xhx.bookread.bean.BookMixAToc;
import com.xhx.bookread.bean.ChapterRead;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.Recommend;
import com.xhx.bookread.configs.Constant;
import com.xhx.bookread.readview.BaseReadView;
import com.xhx.bookread.readview.CacheManager;
import com.xhx.bookread.readview.CollectionsManager;
import com.xhx.bookread.readview.NoAimWidget;
import com.xhx.bookread.readview.OnReadStateChangeListener;
import com.xhx.bookread.readview.ScreenUtils;
import com.xhx.bookread.readview.SettingManager;
import com.xhx.bookread.util.FileUtils;
import com.xhx.bookread.util.LogUtils;
import com.xhx.bookread.view.epubview.EPubReaderAdapter;
import com.xhx.bookread.view.epubview.ReaderCallback;
import com.xhx.bookread.view.epubview.TocListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class ReadEPubActivity1 extends MBaseActivity implements ReaderCallback {

    public static void start(Context context, FilesBean filesBean) {
        Intent intent = new Intent(context, ReadEPubActivity1.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.fromFile(new File(filesBean.getFilePath())));
        intent.putExtra("FilesBean", filesBean);
        context.startActivity(intent);
    }

    //    DirectionalViewpager viewpager;
    View ivMenu;
    TextView tvTitle;

    private EPubReaderAdapter mAdapter;

    private String mFileName;
    private String mFilePath;

    private Book mBook;
    private ArrayList<TOCReference> mTocReferences;
    private List<SpineReference> mSpineReferences;
    public boolean mIsSmilParsed = false;

    private List<BookMixAToc.mixToc.Chapters> mChapterList = new ArrayList<>();
    private ListPopupWindow mTocListPopupWindow;
    private TocListAdapter mTocListAdapter;

    private boolean mIsActionBarVisible = true;
    private int currentChapter;
    FrameLayout flReadWidget;
    private BaseReadView mPageWidget;
    private String bookId;
    private View decodeView;
    private Receiver receiver = new Receiver();
    /**
     * 是否开始阅读章节
     **/
    private boolean startRead = false;
    private Recommend.RecommendBooks recommendBooks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        viewpager = findViewById(R.id.epubViewPager);
        flReadWidget = findViewById(R.id.flReadWidget);
        ivMenu = findViewById(R.id.toolbar_menu);
        tvTitle = findViewById(R.id.toolbar_title);
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu();
            }
        });
        FilesBean filesBean = (FilesBean) getIntent().getSerializableExtra("FilesBean");
        setTitle(filesBean.getFileName());
        decodeView = getWindow().getDecorView();

    }

    @Override
    public void initToolBar() {
        super.initToolBar();
        mCommonToolbar.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mCommonToolbar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        hideToolBarIfVisible();
                    }
                });
        showDialog();
    }

    @Override
    public void initDatas() {
        mFilePath = Uri.decode(getIntent().getDataString().replace("file://", ""));
        mFileName = mFilePath.substring(mFilePath.lastIndexOf("/") + 1, mFilePath.lastIndexOf("."));

        CollectionsManager.getInstance().remove(mFileName);
        // 转存
        File desc = FileUtils.createWifiTranfesFile(mFileName);
        FileUtils.fileChannelCopy(new File(mFilePath), desc);
        // 建立
        recommendBooks = new Recommend.RecommendBooks();
        recommendBooks.isFromSD = true;
        recommendBooks._id = mFileName;
        bookId = mFileName;
        recommendBooks.title = mFileName;
        loadBook(mFilePath, mFileName);
    }

    @Override
    public void configViews() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                loadBook(mFilePath, mFileName);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                initPager();

                initTocList();

                BookMixAToc.mixToc.Chapters chapters = new BookMixAToc.mixToc.Chapters();
                chapters.title = recommendBooks.title;
                mChapterList.add(chapters);
                showChapterRead(null, currentChapter);
            }
        }.execute();


    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_read_epub1;
    }

    private void loadBook(String filePath, String fileName) {

        try {
            // 打开书籍
            EpubReader reader = new EpubReader();
            InputStream is = new FileInputStream(filePath);
            mBook = reader.readEpub(is);
            mTocReferences = (ArrayList<TOCReference>) mBook.getTableOfContents().getTocReferences();
            mSpineReferences = mBook.getSpine().getSpineReferences();
            setSpineReferenceTitle();
            // 解压epub至缓存目录
            FileUtils.unzipFile(filePath, Constant.PATH_EPUB + "/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void showChapterRead(ChapterRead.Chapter data, int chapter) { // 加载章节内容
        if (data != null) {
            CacheManager.getInstance().saveChapterFile(bookId, chapter, data);
        }

        if (!startRead) {
            startRead = true;
            currentChapter = chapter;
            if (!mPageWidget.isPrepared) {
                mPageWidget.init(0);
            } else {
                mPageWidget.jumpToChapter(currentChapter);
            }
            hideDialog();
        }
    }

    private void initPager() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter);
        mPageWidget = new NoAimWidget(this, bookId, mChapterList, new ReadListener());
        flReadWidget.removeAllViews();
        flReadWidget.addView(mPageWidget);
//        viewpager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                mTocListAdapter.setCurrentChapter(position + 1);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//            }
//        });

//        if (mBook != null && mSpineReferences != null && mTocReferences != null) {
//
//            mAdapter = new EPubReaderAdapter(getSupportFragmentManager(),
//                    mSpineReferences, mBook, mFileName, mIsSmilParsed);
//            viewpager.setAdapter(mAdapter);
//        }

        hideDialog();
    }

    private void initTocList() {
        mTocListAdapter = new TocListAdapter(this, mChapterList, "", 1);
        mTocListAdapter.setEpub(true);
        mTocListPopupWindow = new ListPopupWindow(this);
        mTocListPopupWindow.setAdapter(mTocListAdapter);
        mTocListPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mTocListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mTocListPopupWindow.setAnchorView(mCommonToolbar);
        mTocListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTocListPopupWindow.dismiss();
                currentChapter = position + 1;
                mTocListAdapter.setCurrentChapter(currentChapter);
//                viewpager.setCurrentItem(position);
            }
        });
        mTocListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                toolbarAnimateHide();
            }
        });
    }

    private void setSpineReferenceTitle() {
        int srSize = mSpineReferences.size();
        int trSize = mTocReferences.size();
        for (int j = 0; j < srSize; j++) {
            String href = mSpineReferences.get(j).getResource().getHref();
            for (int i = 0; i < trSize; i++) {
                if (mTocReferences.get(i).getResource().getHref().equalsIgnoreCase(href)) {
                    mSpineReferences.get(j).getResource().setTitle(mTocReferences.get(i).getTitle());
                    break;
                } else {
                    mSpineReferences.get(j).getResource().setTitle("");
                }
            }
        }

        for (int i = 0; i < trSize; i++) {
            Resource resource = mTocReferences.get(i).getResource();
            if (resource != null) {
                mChapterList.add(new BookMixAToc.mixToc.Chapters(resource.getTitle(), resource.getHref()));
            }
        }
    }

    @Override
    public String getPageHref(int position) {
        String pageHref = mTocReferences.get(position).getResource().getHref();
        String opfpath = FileUtils.getPathOPF(FileUtils.getEpubFolderPath(mFileName));
        if (FileUtils.checkOPFInRootDirectory(FileUtils.getEpubFolderPath(mFileName))) {
            pageHref = FileUtils.getEpubFolderPath(mFileName) + "/" + pageHref;
        } else {
            pageHref = FileUtils.getEpubFolderPath(mFileName) + "/" + opfpath + "/" + pageHref;
        }
        return pageHref;
    }

    @Override
    public void toggleToolBarVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        } else {
            toolbarAnimateShow(1);
        }
    }

    @Override
    public void hideToolBarIfVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        }
    }

    private void toolbarAnimateShow(final int verticalOffset) {
        showStatusBar();
        mCommonToolbar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        toolbarSetElevation(verticalOffset == 0 ? 0 : 1);
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsActionBarVisible) {
                            toolbarAnimateHide();
                        }
                    }
                });
            }
        }, 10000);

        mIsActionBarVisible = true;
    }

    private void toolbarAnimateHide() {
        if (mIsActionBarVisible) {
            mCommonToolbar.animate()
                    .translationY(-mCommonToolbar.getHeight())
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(180)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toolbarSetElevation(0);
                            hideStatusBar();
                            if (mTocListPopupWindow != null && mTocListPopupWindow.isShowing()) {
                                mTocListPopupWindow.dismiss();
                            }
                        }
                    });
            mIsActionBarVisible = false;
        }
    }

    private void toolbarSetElevation(float elevation) {
        mCommonToolbar.setElevation(elevation);
    }

    public void showMenu() {
        if (!mTocListPopupWindow.isShowing()) {
            mTocListPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
            mTocListPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            mTocListPopupWindow.show();
            mTocListPopupWindow.setSelection(currentChapter - 1);
            mTocListPopupWindow.getListView().setFastScrollEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mTocListPopupWindow != null && mTocListPopupWindow.isShowing()) {
            mTocListPopupWindow.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    private class ReadListener implements OnReadStateChangeListener {
        @Override
        public void onChapterChanged(int chapter) {
            LogUtils.i("onChapterChanged:" + chapter);
            currentChapter = chapter;
            mTocListAdapter.setCurrentChapter(currentChapter);
            // 加载前一节 与 后三节
            for (int i = chapter - 1; i <= chapter + 3 && i <= mChapterList.size(); i++) {
                if (i > 0 && i != chapter
                        && CacheManager.getInstance().getChapterFile(bookId, i) == null) {
//                    mPresenter.getChapterRead(mChapterList.get(i - 1).link, i);
                }
            }
        }

        @Override
        public void onPageChanged(int chapter, int page) {
            LogUtils.i("onPageChanged:" + chapter + "-" + page);
        }

        @Override
        public void onLoadChapterFailure(int chapter) {
            LogUtils.i("onLoadChapterFailure:" + chapter);
            startRead = false;
//            if (CacheManager.getInstance().getChapterFile(bookId, chapter) == null)
//                mPresenter.getChapterRead(mChapterList.get(chapter - 1).link, chapter);
        }

        @Override
        public void onCenterClick() {
            LogUtils.i("onCenterClick");
            toggleReadBar();
        }

        @Override
        public void onFlip() {
            hideReadBar();
        }
    }

    private synchronized void toggleReadBar() { // 切换工具栏 隐藏/显示 状态
        if (isVisible(mCommonToolbar)) {
            hideReadBar();
        } else {
            showReadBar();
        }
    }

    private synchronized void hideReadBar() {
        toolbarAnimateHide();
        decodeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    private synchronized void showReadBar() { // 显示工具栏
        toolbarAnimateShow(1);
        decodeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (SettingManager.getInstance().isVolumeFlipEnable()) {
                mPageWidget.nextPage();
                return true;// 防止翻页有声音
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (SettingManager.getInstance().isVolumeFlipEnable()) {
                mPageWidget.prePage();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPageWidget != null) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    int level = intent.getIntExtra("level", 0);
                    mPageWidget.setBattery(100 - level);
                } else if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                    mPageWidget.setTime(sdf.format(new Date()));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            LogUtils.e("Receiver not registered");
        }

    }
    private void calcFontSize(int progress) {
        // progress range 1 - 10
        if (progress >= 0 && progress <= 10) {
            mPageWidget.setFontSize(ScreenUtils.dpToPxInt(12 + 1.7f * progress));
        }
    }
}
