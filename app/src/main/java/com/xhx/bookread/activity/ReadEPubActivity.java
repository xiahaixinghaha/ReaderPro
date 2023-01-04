package com.xhx.bookread.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;

import com.example.pdfsearch.R;
import com.xhx.bookread.bean.BookMixAToc;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.configs.Constant;
import com.xhx.bookread.util.FileUtils;
import com.xhx.bookread.view.epubview.DirectionalViewpager;
import com.xhx.bookread.view.epubview.EPubReaderAdapter;
import com.xhx.bookread.view.epubview.ReaderCallback;
import com.xhx.bookread.view.epubview.TocListAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class ReadEPubActivity extends MBaseActivity implements ReaderCallback {

    public static void start(Context context, FilesBean filesBean) {
        Intent intent = new Intent(context, ReadEPubActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.fromFile(new File(filesBean.getFilePath())));
        intent.putExtra("FilesBean", filesBean);
        context.startActivity(intent);
    }

    DirectionalViewpager viewpager;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewpager = findViewById(R.id.epubViewPager);
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
    }

    @Override
    public void configViews() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                loadBook();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                initPager();

                initTocList();
            }
        }.execute();

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_read_epub;
    }

    private void loadBook() {

        try {
            // 打开书籍
            EpubReader reader = new EpubReader();
            InputStream is = new FileInputStream(mFilePath);
            mBook = reader.readEpub(is);
            mTocReferences = (ArrayList<TOCReference>) mBook.getTableOfContents().getTocReferences();
            mSpineReferences = mBook.getSpine().getSpineReferences();
            setSpineReferenceTitle();
            // 解压epub至缓存目录
            FileUtils.unzipFile(mFilePath, Constant.PATH_EPUB + "/" + mFileName);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPager() {

        viewpager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTocListAdapter.setCurrentChapter(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (mBook != null && mSpineReferences != null && mTocReferences != null) {

            mAdapter = new EPubReaderAdapter(getSupportFragmentManager(),
                    mSpineReferences, mBook, mFileName, mIsSmilParsed);
            viewpager.setAdapter(mAdapter);
        }

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
                viewpager.setCurrentItem(position);
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
}
