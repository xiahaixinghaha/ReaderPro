package com.xhx.bookread.activity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.pdfsearch.R;
import com.xhx.bookread.adapter.BottomTuiJianAdapter;
import com.xhx.bookread.adapter.HomeFeiLeiAdapter;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.FilesBeanText;
import com.xhx.bookread.bean.FolderBean;
import com.xhx.bookread.configs.DatabaseConfig;
import com.xhx.bookread.util.PermissionUtils;
import com.xhx.bookread.util.PublicUtil;
import com.xhx.bookread.util.SpUtils;
import com.xhx.bookread.view.ViewHomeItem;

import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.epub.Main;

public class MainActivity extends MBaseActivity {
    String TAG = "MainActivity";
    List<FolderBean> folderBeans = new ArrayList<>();
    int REQUEST_CODE_CONTACT = 101;
    Button btnRequestPermission;
    LinearLayout llContent;
    ImageView ivPic;
    ViewHomeItem vhi11, vhi12, vhi13, vhi21, vhi22, vhi23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnRequestPermission = findViewById(R.id.btnRequestPermission);
        llContent = findViewById(R.id.llContent);
        ivPic = findViewById(R.id.ivPic);
        vhi11 = findViewById(R.id.vhi11);
        vhi12 = findViewById(R.id.vhi12);
        vhi13 = findViewById(R.id.vhi13);
        vhi21 = findViewById(R.id.vhi21);
        vhi22 = findViewById(R.id.vhi22);
        vhi23 = findViewById(R.id.vhi23);


        btnRequestPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });
        requestPermission();
        rectRoundBitmap();
    }

    private void rectRoundBitmap() {
        //得到资源文件的BitMap
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.mipmap.home_logo);
        //创建RoundedBitmapDrawable对象
        RoundedBitmapDrawable roundImg = RoundedBitmapDrawableFactory.create(getResources(), image);
        //抗锯齿
        roundImg.setAntiAlias(true);
        //设置圆角半径
        roundImg.setCornerRadius(30);
        //设置显示图片
        ivPic.setImageDrawable(roundImg);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    private void requestPermission() {
        PermissionUtils.requestStoragePermission(this, new PermissionUtils.RequestCallBack() {
            @Override
            public void callBack(boolean isHave) {
                if (isHave) {
                    initBegin();
                } else {
                    btnRequestPermission.setVisibility(View.VISIBLE);
                    llContent.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initBegin() {
        btnRequestPermission.setVisibility(View.GONE);
        llContent.setVisibility(View.VISIBLE);
        initView();
        getData();
    }


    private void getData() {
        folderBeans.clear();
        List<FolderBean> list = FolderBean.listAll(FolderBean.class);

        final int[] itemBgRes = new int[]{R.drawable.bg_home_item_1, R.drawable.bg_home_item_2, R.drawable.bg_home_item_3,
                R.drawable.bg_home_item_4, R.drawable.bg_home_item_5, R.drawable.bg_home_item_6};
        final int[] iconRes = new int[]{R.mipmap.png_fenlei_1, R.mipmap.png_fenlei_2, R.mipmap.png_fenlei_3,
                R.mipmap.png_fenlei_4, R.mipmap.png_fenlei_5, R.mipmap.png_fenlei_6};

        final ViewHomeItem[] items = new ViewHomeItem[]{vhi11, vhi12, vhi13, vhi21, vhi22, vhi23};
        for (ViewHomeItem item : items) {
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FolderBean folderBean = item.getFolderBean();
                    FenLeiActivity.startFenLeiActivity(MainActivity.this, folderBean);
                }
            });
        }

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                ViewHomeItem viewHomeItem = items[i % items.length];
                int imgRes = iconRes[i % iconRes.length];
                int bgRes = itemBgRes[i % itemBgRes.length];
                viewHomeItem.setTxtAndImg(list.get(i).getFolderName(), imgRes, bgRes);
                viewHomeItem.bindData(list.get(i));
                viewHomeItem.setVisibility(View.VISIBLE);
            }
//            folderBeans.addAll(list);
//            feiLeiAdapter.notifyDataSetChanged();
        }
    }

    private void initView() {

        RecyclerView rvBottom = findViewById(R.id.rvBottom);
        rvBottom.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<FilesBeanText> books = FilesBeanText.findWithQuery(FilesBeanText.class, DatabaseConfig.getQueryRandom(DatabaseConfig.FILES_TAB_NAME_TEXT, 10));
        BottomTuiJianAdapter bottomTuiJianAdapter = new BottomTuiJianAdapter(books);
        bottomTuiJianAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FilesBeanText filesBeanText = (FilesBeanText) adapter.getItem(position);
                FilesBean filesBean = new FilesBean(filesBeanText.getFilePath(), filesBeanText.getFileName(), filesBeanText.getFileType(),
                        filesBeanText.getAddDate(), filesBeanText.getClassify(), filesBeanText.getFolderIndex());
                PublicUtil.openFile(MainActivity.this, filesBean);
            }
        });
        rvBottom.setAdapter(bottomTuiJianAdapter);

        findViewById(R.id.lookAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FenLeiActivity.startFenLeiActivity(MainActivity.this, null);
            }
        });

        findViewById(R.id.lookAll1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FenLeiActivity.startFenLeiActivity(MainActivity.this, null);
            }
        });
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getData();
    }

    final int mDuraction = 2000; // 两次返回键之间的时间差
    long mLastTime = 0; // 最后一次按back键的时刻

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {// 截获back事件
            exitApp();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void exitApp() {
        if (System.currentTimeMillis() - mLastTime > mDuraction) {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_LONG).show();
            mLastTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}