/**
 * Copyright 2016 JustWayward Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xhx.bookread.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;

import com.example.pdfsearch.R;
import com.xhx.bookread.adapter.BookMarkAdapter;
import com.xhx.bookread.bean.BookMark;
import com.xhx.bookread.bean.BookMixAToc;
import com.xhx.bookread.bean.ChapterRead;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.Recommend;
import com.xhx.bookread.configs.Constant;
import com.xhx.bookread.contract.BookReadContract;
import com.xhx.bookread.readview.AppComponent;
import com.xhx.bookread.readview.BaseReadView;
import com.xhx.bookread.readview.CacheManager;
import com.xhx.bookread.readview.CollectionsManager;
import com.xhx.bookread.readview.EventManager;
import com.xhx.bookread.readview.NoAimWidget;
import com.xhx.bookread.readview.OnReadStateChangeListener;
import com.xhx.bookread.readview.OverlappedWidget;
import com.xhx.bookread.readview.PageWidget;
import com.xhx.bookread.readview.ReadTheme;
import com.xhx.bookread.readview.ReadThemeAdapter;
import com.xhx.bookread.readview.ScreenUtils;
import com.xhx.bookread.readview.SettingManager;
import com.xhx.bookread.readview.SharedPreferencesUtil;
import com.xhx.bookread.readview.ThemeManager;
import com.xhx.bookread.util.AppUtils;
import com.xhx.bookread.util.FileUtils;
import com.xhx.bookread.util.LogUtils;
import com.xhx.bookread.util.ToastUtils;
import com.xhx.bookread.view.epubview.TocListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by lfh on 2016/9/18.
 */
public class ReadActivity extends MBaseActivity implements BookReadContract.View {

    ImageView mIvBack;
    TextView mTvBookReadReading;
    TextView mTvBookReadCommunity;
    TextView mTvBookReadChangeSource;
    TextView mTvBookReadSource;

    FrameLayout flReadWidget;

    LinearLayout mLlBookReadTop;
    TextView mTvBookReadTocTitle;
    TextView mTvBookReadMode;
    TextView mTvBookReadSettings;
    TextView mTvBookReadDownload;
    TextView mTvBookReadToc;
    LinearLayout mLlBookReadBottom;
    RelativeLayout mRlBookReadRoot;
    TextView mTvDownloadProgress;
    LinearLayout rlReadAaSet;
    ImageView ivBrightnessMinus;
    SeekBar seekbarLightness;
    ImageView ivBrightnessPlus;
    TextView tvFontsizeMinus;
    SeekBar seekbarFontSize;
    TextView tvFontsizePlus;

    LinearLayout rlReadMark;
    TextView tvAddMark;
    ListView lvMark;

    CheckBox cbVolume;
    CheckBox cbAutoBrightness;
    GridView gvTheme;

    private View decodeView;

//    @Inject
//    BookReadPresenter mPresenter;

    private List<BookMixAToc.mixToc.Chapters> mChapterList = new ArrayList<>();
    private ListPopupWindow mTocListPopupWindow;
    private TocListAdapter mTocListAdapter;

    private List<BookMark> mMarkList;
    private BookMarkAdapter mMarkAdapter;

    private int currentChapter = 1;

    /**
     * 是否开始阅读章节
     **/
    private boolean startRead = false;

    /**
     * 朗读 播放器
     */
//    private TTSPlayer mTtsPlayer;
//    private TtsConfig ttsConfig;

    private BaseReadView mPageWidget;
    private int curTheme = -1;
    private List<ReadTheme> themes;
    private ReadThemeAdapter gvAdapter;
    private Receiver receiver = new Receiver();
    private IntentFilter intentFilter = new IntentFilter();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public static final String INTENT_BEAN = "recommendBooksBean";
    public static final String INTENT_SD = "isFromSD";

    private Recommend.RecommendBooks recommendBooks;
    private String bookId;

    private boolean isAutoLightness = false; // 记录其他页面是否自动调整亮度
    private boolean isFromSD = false;

    //添加收藏需要，所以跳转的时候传递整个实体类
    public static void startActivity(Context context, Recommend.RecommendBooks recommendBooks) {
        startActivity(context, recommendBooks, false);
    }

    public static void startActivity(Context context, Recommend.RecommendBooks recommendBooks, boolean isFromSD) {
        context.startActivity(new Intent(context, ReadActivity.class)
                .putExtra(INTENT_BEAN, recommendBooks)
                .putExtra(INTENT_SD, isFromSD));
    }

    public static void start(Context context, FilesBean filesBean) {
        Intent intent = new Intent(context, ReadActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.fromFile(new File(filesBean.getFilePath())));
        intent.putExtra("FilesBean", filesBean);

        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        statusBarColor = ContextCompat.getColor(this, R.color.reader_menu_bg_color);
        return R.layout.activity_read;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void initToolBar() {
        initView();
    }

    private void initView() {
        mIvBack = findViewById(R.id.ivBack);
        mTvBookReadReading = findViewById(R.id.tvBookReadReading);
        mTvBookReadCommunity = findViewById(R.id.tvBookReadCommunity);
        mTvBookReadChangeSource = findViewById(R.id.tvBookReadIntroduce);
        mTvBookReadSource = findViewById(R.id.tvBookReadSource);
        flReadWidget = findViewById(R.id.flReadWidget);
        mLlBookReadTop = findViewById(R.id.llBookReadTop);
        mTvBookReadTocTitle = findViewById(R.id.tvBookReadTocTitle);
        mTvBookReadMode = findViewById(R.id.tvBookReadMode);

        mTvBookReadSettings = findViewById(R.id.tvBookReadSettings);

        mTvBookReadDownload = findViewById(R.id.tvBookReadDownload);
        mTvBookReadToc = findViewById(R.id.tvBookReadToc);
        mLlBookReadBottom = findViewById(R.id.llBookReadBottom);
        mRlBookReadRoot = findViewById(R.id.rlBookReadRoot);
        mTvDownloadProgress = findViewById(R.id.tvDownloadProgress);
        rlReadAaSet = findViewById(R.id.rlReadAaSet);
        ivBrightnessMinus = findViewById(R.id.ivBrightnessMinus);

        seekbarLightness = findViewById(R.id.seekbarLightness);
        ivBrightnessPlus = findViewById(R.id.ivBrightnessPlus);

        tvFontsizeMinus = findViewById(R.id.tvFontsizeMinus);
        tvFontsizeMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calcFontSize(seekbarFontSize.getProgress() - 1);
            }
        });
        seekbarFontSize = findViewById(R.id.seekbarFontSize);
        tvFontsizePlus = findViewById(R.id.tvFontsizePlus);
        tvFontsizePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calcFontSize(seekbarFontSize.getProgress() + 1);
            }
        });
        rlReadMark = findViewById(R.id.rlReadMark);
        tvAddMark = findViewById(R.id.tvAddMark);
        lvMark = findViewById(R.id.lvMark);

        cbVolume = findViewById(R.id.cbVolume);
        cbAutoBrightness = findViewById(R.id.cbAutoBrightness);
        gvTheme = findViewById(R.id.gvTheme);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTocListPopupWindow.isShowing()) {
                    mTocListPopupWindow.dismiss();
                } else {
                    finish();
                }
            }
        });
        mTvBookReadReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mTvBookReadMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gone(rlReadAaSet, rlReadMark);

                boolean isNight = !SharedPreferencesUtil.getInstance().getBoolean(Constant.ISNIGHT, false);
                changedMode(isNight, -1);
            }
        });

        mTvBookReadSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isVisible(mLlBookReadBottom)) {
                    if (isVisible(rlReadAaSet)) {
                        gone(rlReadAaSet);
                    } else {
                        visible(rlReadAaSet);
                        gone(rlReadMark);
                    }
                }
            }
        });

        findViewById(R.id.tvBookMark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (isVisible(mLlBookReadBottom)) {
                    if (isVisible(rlReadMark)) {
                        gone(rlReadMark);
                    } else {
                        gone(rlReadAaSet);

                        updateMark();

                        visible(rlReadMark);
                    }
                }
            }
        });

        ivBrightnessMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curBrightness = SettingManager.getInstance().getReadBrightness();
                if (curBrightness > 5 && !SettingManager.getInstance().isAutoBrightness()) {
                    seekbarLightness.setProgress((curBrightness = curBrightness - 2));
                    ScreenUtils.saveScreenBrightnessInt255(curBrightness, ReadActivity.this);
                }
            }
        });

        ivBrightnessPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curBrightness = SettingManager.getInstance().getReadBrightness();
                if (!SettingManager.getInstance().isAutoBrightness()) {
                    seekbarLightness.setProgress((curBrightness = curBrightness + 2));
                    ScreenUtils.saveScreenBrightnessInt255(curBrightness, ReadActivity.this);
                }
            }
        });
        findViewById(R.id.tvClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingManager.getInstance().clearBookMarks(bookId);

                updateMark();
            }
        });

//        tvAddMark.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int[] readPos = mPageWidget.getReadPos();
//                BookMark mark = new BookMark();
//                mark.chapter = readPos[0];
//                mark.startPos = readPos[1];
//                mark.endPos = readPos[2];
//                if (mark.chapter >= 1 && mark.chapter <= mChapterList.size()) {
//                    mark.title = mChapterList.get(mark.chapter - 1).title;
//                }
//                mark.desc = mPageWidget.getHeadLine();
//                if (SettingManager.getInstance().addBookMark(bookId, mark)) {
//                    ToastUtils.showSingleToast("添加书签成功");
//                    updateMark();
//                } else {
//                    ToastUtils.showSingleToast("书签已存在");
//                }
//            }
//        });

    }

    @Override
    public void initDatas() {
//        recommendBooks = (Recommend.RecommendBooks) getIntent().getSerializableExtra(INTENT_BEAN);
        FilesBean filesBean = (FilesBean) getIntent().getSerializableExtra("FilesBean");

        recommendBooks = new Recommend.RecommendBooks();
        recommendBooks.path = filesBean.getFilePath();

        bookId = recommendBooks._id;
        isFromSD = getIntent().getBooleanExtra(INTENT_SD, false);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            String mFilePath = Uri.decode(getIntent().getDataString().replace("file://", ""));
            String fileName;
            if (mFilePath.lastIndexOf(".") > mFilePath.lastIndexOf("/")) {
                fileName = mFilePath.substring(mFilePath.lastIndexOf("/") + 1, mFilePath.lastIndexOf("."));
            } else {
                fileName = mFilePath.substring(mFilePath.lastIndexOf("/") + 1);
            }

            CollectionsManager.getInstance().remove(fileName);
            // 转存
            File desc = FileUtils.createWifiTranfesFile(fileName);
            FileUtils.fileChannelCopy(new File(mFilePath), desc);
            // 建立
            recommendBooks = new Recommend.RecommendBooks();
            recommendBooks.isFromSD = true;
            recommendBooks._id = fileName;
            bookId=fileName;
            recommendBooks.title = fileName;
            loadBook(mFilePath,fileName);

            isFromSD = true;
        }
//        EventBus.getDefault().register(this);
        showDialog();

        mTvBookReadTocTitle.setText(recommendBooks.title);

//        mTtsPlayer = TTSPlayerUtils.getTTSPlayer();
//        ttsConfig = TTSPlayerUtils.getTtsConfig();

        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);

        CollectionsManager.getInstance().setRecentReadingTime(bookId);
        Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        //延迟1秒刷新书架
                        EventManager.refreshCollectionList();
                    }
                });
    }
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences;
    private List<SpineReference> mSpineReferences;
    private void loadBook(String filePath,String fileName) {

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
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void configViews() {
        hideStatusBar();
        decodeView = getWindow().getDecorView();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLlBookReadTop.getLayoutParams();
        params.topMargin = ScreenUtils.getStatusBarHeight(this) - 2;
        mLlBookReadTop.setLayoutParams(params);

        initTocList();

        initAASet();

        initPagerWidget();

//        mPresenter.attachView(this);
        // 本地收藏  直接打开
        if (isFromSD) {
            BookMixAToc.mixToc.Chapters chapters = new BookMixAToc.mixToc.Chapters();
            chapters.title = recommendBooks.title;
            mChapterList.add(chapters);
            showChapterRead(null, currentChapter);
            //本地书籍隐藏社区、简介、缓存按钮
            gone(mTvBookReadCommunity, mTvBookReadChangeSource, mTvBookReadDownload);
            return;
        }
//        mPresenter.getBookMixAToc(bookId, "chapters");
//        showBookToc(mChapterList);
    }


    private void initTocList() {
        mTocListAdapter = new TocListAdapter(this, mChapterList, bookId, currentChapter);
        mTocListPopupWindow = new ListPopupWindow(this);
        mTocListPopupWindow.setAdapter(mTocListAdapter);
        mTocListPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mTocListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mTocListPopupWindow.setAnchorView(mLlBookReadTop);
        mTocListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTocListPopupWindow.dismiss();
                currentChapter = position + 1;
                mTocListAdapter.setCurrentChapter(currentChapter);
                startRead = false;
                showDialog();
                readCurrentChapter();
                hideReadBar();
            }
        });
        mTocListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                gone(mTvBookReadTocTitle);
                visible(mTvBookReadReading, mTvBookReadCommunity, mTvBookReadChangeSource);
            }
        });
    }

    /**
     * 时刻监听系统亮度改变事件
     */
    private ContentObserver Brightness = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //LogUtils.d("BrightnessOnChange:" + ScreenUtils.getScreenBrightnessInt255());
            if (!ScreenUtils.isAutoBrightness(ReadActivity.this)) {
                seekbarLightness.setProgress(ScreenUtils.getScreenBrightness());
            }
        }
    };


    private void initAASet() {
        curTheme = SettingManager.getInstance().getReadTheme();
        ThemeManager.setReaderTheme(curTheme, mRlBookReadRoot);

        seekbarFontSize.setMax(10);
        //int fontSizePx = SettingManager.getInstance().getReadFontSize(bookId);
        int fontSizePx = SettingManager.getInstance().getReadFontSize();
        int progress = (int) ((ScreenUtils.pxToDpInt(fontSizePx) - 12) / 1.7f);
        seekbarFontSize.setProgress(progress);
        seekbarFontSize.setOnSeekBarChangeListener(new SeekBarChangeListener());

        seekbarLightness.setMax(100);
        seekbarLightness.setOnSeekBarChangeListener(new SeekBarChangeListener());
        seekbarLightness.setProgress(ScreenUtils.getScreenBrightness());
        isAutoLightness = ScreenUtils.isAutoBrightness(this);


        this.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true, Brightness);

        if (SettingManager.getInstance().isAutoBrightness()) {
            startAutoLightness();
        } else {
            stopAutoLightness();
        }

        cbVolume.setChecked(SettingManager.getInstance().isVolumeFlipEnable());
        cbVolume.setOnCheckedChangeListener(new ChechBoxChangeListener());

        cbAutoBrightness.setChecked(SettingManager.getInstance().isAutoBrightness());
        cbAutoBrightness.setOnCheckedChangeListener(new ChechBoxChangeListener());

        gvAdapter = new ReadThemeAdapter(this, (themes = ThemeManager.getReaderThemeData(curTheme)), curTheme);
        gvTheme.setAdapter(gvAdapter);
        gvTheme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < themes.size() - 1) {
                    changedMode(false, position);
                } else {
                    changedMode(true, position);
                }
            }
        });
    }

    private void initPagerWidget() {
        mPageWidget = new NoAimWidget(this, bookId, mChapterList, new ReadListener());
//        switch (SharedPreferencesUtil.getInstance().getInt(Constant.FLIP_STYLE, 0)) {
//            case 0:
//                mPageWidget = new PageWidget(this, bookId, mChapterList, new ReadListener());
//                break;
//            case 1:
//                mPageWidget = new OverlappedWidget(this, bookId, mChapterList, new ReadListener());
//                break;
//            case 2:
//                mPageWidget = new NoAimWidget(this, bookId, mChapterList, new ReadListener());
//        }

        registerReceiver(receiver, intentFilter);
        if (SharedPreferencesUtil.getInstance().getBoolean(Constant.ISNIGHT, false)) {
            mPageWidget.setTextColor(ContextCompat.getColor(this, R.color.chapter_content_night),
                    ContextCompat.getColor(this, R.color.chapter_title_night));
        }
        flReadWidget.removeAllViews();
        flReadWidget.addView(mPageWidget);
    }

    /**
     * 加载章节列表
     *
     * @param list
     */
    @Override
    public void showBookToc(List<BookMixAToc.mixToc.Chapters> list) {
        mChapterList.clear();
        mChapterList.addAll(list);
        readCurrentChapter();
    }

    /**
     * 获取当前章节。章节文件存在则直接阅读，不存在则请求
     */
    public void readCurrentChapter() {
        if (CacheManager.getInstance().getChapterFile(bookId, currentChapter) != null) {
            showChapterRead(null, currentChapter);
        }
        else {

        }
    }


    public void merginAllChapterToFile() {
//        mPresenter.merginAllBook(recommendBooks, mChapterList);
    }


    @Override
    public synchronized void showChapterRead(ChapterRead.Chapter data, int chapter) { // 加载章节内容
        if (data != null) {
            CacheManager.getInstance().saveChapterFile(bookId, chapter, data);
        }

        if (!startRead) {
            startRead = true;
            currentChapter = chapter;
            if (!mPageWidget.isPrepared) {
                mPageWidget.init(curTheme);
            } else {
                mPageWidget.jumpToChapter(currentChapter);
            }
            hideDialog();
        }
    }

    @Override
    public void netError(int chapter) {
        hideDialog();//防止因为网络问题而出现dialog不消失
//        if (Math.abs(chapter - currentChapter) <= 1) {
//            ToastUtils.showToast(R.string.net_error);
//        }
    }

    @Override
    public void showError() {
        hideDialog();
    }

    @Override
    public void complete() {
        hideDialog();
    }

    private synchronized void hideReadBar() {
        gone(mTvDownloadProgress, mLlBookReadBottom, mLlBookReadTop, rlReadAaSet, rlReadMark);
        hideStatusBar();
        decodeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    private synchronized void showReadBar() { // 显示工具栏
        visible(mLlBookReadBottom, mLlBookReadTop);
        showStatusBar();
        decodeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private synchronized void toggleReadBar() { // 切换工具栏 隐藏/显示 状态
        if (isVisible(mLlBookReadTop)) {
            hideReadBar();
        } else {
            showReadBar();
        }
    }

    /***************Title Bar*****************/


    private void changedMode(boolean isNight, int position) {
        SharedPreferencesUtil.getInstance().putBoolean(Constant.ISNIGHT, isNight);
        AppCompatDelegate.setDefaultNightMode(isNight ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        if (position >= 0) {
            curTheme = position;
        } else {
            curTheme = SettingManager.getInstance().getReadTheme();
        }
        gvAdapter.select(curTheme);

        mPageWidget.setTheme(isNight ? ThemeManager.NIGHT : curTheme);
        mPageWidget.setTextColor(ContextCompat.getColor(mContext, isNight ? R.color.chapter_content_night : R.color.chapter_content_day),
                ContextCompat.getColor(mContext, isNight ? R.color.chapter_title_night : R.color.chapter_title_day));

        mTvBookReadMode.setText(getString(isNight ? R.string.book_read_mode_day_manual_setting
                : R.string.book_read_mode_night_manual_setting));
        Drawable drawable = ContextCompat.getDrawable(this, isNight ? R.mipmap.ic_menu_mode_day_manual
                : R.mipmap.ic_menu_mode_night_manual);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mTvBookReadMode.setCompoundDrawables(null, drawable, null, null);

        ThemeManager.setReaderTheme(curTheme, mRlBookReadRoot);
    }

    /***************Setting Menu*****************/


    private void updateMark() {
        if (mMarkAdapter == null) {
            mMarkAdapter = new BookMarkAdapter(this, new ArrayList<BookMark>());
            lvMark.setAdapter(mMarkAdapter);
            lvMark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BookMark mark = mMarkAdapter.getData(position);
                    if (mark != null) {
                        mPageWidget.setPosition(new int[]{mark.chapter, mark.startPos, mark.endPos});
                        hideReadBar();
                    } else {
                        ToastUtils.showSingleToast("书签无效");
                    }
                }
            });
        }
        mMarkAdapter.clear();

        mMarkList = SettingManager.getInstance().getBookMarks(bookId);
        if (mMarkList != null && mMarkList.size() > 0) {
            Collections.reverse(mMarkList);
            mMarkAdapter.addAll(mMarkList);
        }
    }

    /***************Event*****************/

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void showDownProgress(DownloadProgress progress) {
//        if (bookId.equals(progress.bookId)) {
//            if (isVisible(mLlBookReadBottom)) { // 如果工具栏显示，则进度条也显示
//                visible(mTvDownloadProgress);
//                // 如果之前缓存过，就给提示
//                mTvDownloadProgress.setText(progress.message);
//            } else {
//                gone(mTvDownloadProgress);
//            }
//        }
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void downloadMessage(final DownloadMessage msg) {
//        if (isVisible(mLlBookReadBottom)) { // 如果工具栏显示，则进度条也显示
//            if (bookId.equals(msg.bookId)) {
//                visible(mTvDownloadProgress);
//                mTvDownloadProgress.setText(msg.message);
//                if (msg.isComplete) {
//                    mTvDownloadProgress.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            gone(mTvDownloadProgress);
//                        }
//                    }, 2500);
//                }
//            }
//        }
//    }

    /**
     * 显示加入书架对话框
     *
     * @param bean
     */
    private void showJoinBookShelfDialog(final Recommend.RecommendBooks bean) {
//        new AlertDialog.Builder(mContext)
//                .setTitle(getString(R.string.book_read_add_book))
//                .setMessage(getString(R.string.book_read_would_you_like_to_add_this_to_the_book_shelf))
//                .setPositiveButton(getString(R.string.book_read_join_the_book_shelf), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        bean.recentReadingTime = FormatUtils.getCurrentTimeString(FormatUtils.FORMAT_DATE_TIME);
//                        CollectionsManager.getInstance().add(bean);
//                        finish();
//                    }
//                })
//                .setNegativeButton(getString(R.string.book_read_not), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        finish();
//                    }
//                })
//                .create()
//                .show();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case 1:
//                if (resultCode == RESULT_OK) {
//                    BookSource bookSource = (BookSource) data.getSerializableExtra("source");
//                    bookId = bookSource._id;
//                }
//                //mPresenter.getBookMixAToc(bookId, "chapters");
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mTocListPopupWindow != null && mTocListPopupWindow.isShowing()) {
                    mTocListPopupWindow.dismiss();
                    gone(mTvBookReadTocTitle);
                    visible(mTvBookReadReading, mTvBookReadCommunity, mTvBookReadChangeSource);
                    return true;
                } else if (isVisible(rlReadAaSet)) {
                    gone(rlReadAaSet);
                    return true;
                } else if (isVisible(mLlBookReadBottom)) {
                    hideReadBar();
                    return true;
                } else if (!CollectionsManager.getInstance().isCollected(bookId)) {
                    showJoinBookShelfDialog(recommendBooks);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                toggleReadBar();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (SettingManager.getInstance().isVolumeFlipEnable()) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (SettingManager.getInstance().isVolumeFlipEnable()) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PLAYING)
//            mTtsPlayer.stop();

        EventManager.refreshCollectionIcon();
        EventManager.refreshCollectionList();
        EventBus.getDefault().unregister(this);

        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            LogUtils.e("Receiver not registered");
        }

//        if (isAutoLightness) {
//            ScreenUtils.startAutoBrightness(ReadActivity.this);
//        } else {
//            ScreenUtils.stopAutoBrightness(ReadActivity.this);
//        }

//        if (mPresenter != null) {
//            mPresenter.detachView();
//        }

        // 观察内存泄漏情况
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

    private class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == seekbarFontSize.getId() && fromUser) {
                calcFontSize(progress);
            } else if (seekBar.getId() == seekbarLightness.getId() && fromUser
                    && !SettingManager.getInstance().isAutoBrightness()) { // 非自动调节模式下 才可调整屏幕亮度
                ScreenUtils.saveScreenBrightnessInt100(progress, ReadActivity.this);
                //SettingManager.getInstance().saveReadBrightness(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class ChechBoxChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == cbVolume.getId()) {
                SettingManager.getInstance().saveVolumeFlipEnable(isChecked);
            } else if (buttonView.getId() == cbAutoBrightness.getId()) {
                if (isChecked) {
                    startAutoLightness();
                } else {
                    stopAutoLightness();
                    ScreenUtils.saveScreenBrightnessInt255(ScreenUtils.getScreenBrightnessInt255(), AppUtils.getAppContext());
                }
            }
        }
    }

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

    private void startAutoLightness() {
        SettingManager.getInstance().saveAutoBrightness(true);
        ScreenUtils.startAutoBrightness(ReadActivity.this);
        seekbarLightness.setEnabled(false);
    }

    private void stopAutoLightness() {
        SettingManager.getInstance().saveAutoBrightness(false);
//        ScreenUtils.stopAutoBrightness(ReadActivity.this);
        seekbarLightness.setProgress((int) (ScreenUtils.getScreenBrightnessInt255() / 255.0F * 100));
        seekbarLightness.setEnabled(true);
    }

    private void calcFontSize(int progress) {
        // progress range 1 - 10
        if (progress >= 0 && progress <= 10) {
            seekbarFontSize.setProgress(progress);
            mPageWidget.setFontSize(ScreenUtils.dpToPxInt(12 + 1.7f * progress));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showDownProgress() {

    }

}
