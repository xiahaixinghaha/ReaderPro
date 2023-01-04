package com.xhx.bookread.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pdfsearch.R;
import com.xhx.bookread.util.PDFUtil;
import com.tencent.smtt.sdk.TbsReaderView;
import com.tencent.smtt.sdk.TbsReaderView.ReaderCallback;

import java.io.File;

/**
 * @作者: njb
 * @时间: 2019/9/11 20:31
 * @描述: pdf文件预览
 */
public class FileDisplayActivity extends Activity implements ReaderCallback,
        OnClickListener {
    private TextView tv_title;
    private TbsReaderView mTbsReaderView;
    private TextView tv_download;
    private RelativeLayout rl_tbsView;    //rl_tbsView为装载TbsReaderView的视图
    private ProgressBar progressBar_download;
    private DownloadManager mDownloadManager;
    private long mRequestId;
    private DownloadObserver mDownloadObserver;
    private String mFileUrl = "", mFileName, fileName;//文件url 由文件url截取的文件名 上个页面传过来用于显示的文件名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pdf_preview);
        findViewById();
        getFileUrlByIntent();
        mTbsReaderView = new TbsReaderView(this, this);
        rl_tbsView.addView(mTbsReaderView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if ((mFileUrl == null) || (mFileUrl.length() <= 0)) {
            Toast.makeText(FileDisplayActivity.this, "获取文件url出错了",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mFileName = PDFUtil.parseName(mFileUrl);
        {
            //如果是下载过的文件或者本地路径下的文件直接打开
            if (isLocalExist() || mFileUrl.contains("storage")) {
                tv_download.setText("打开文件");
                tv_download.setVisibility(View.GONE);
                displayFile();
            } else {//如果不是本地文件也没有下载过路径包含http就去下载
                if (!mFileUrl.contains("http")) {
                    new AlertDialog.Builder(FileDisplayActivity.this)
                            .setTitle("温馨提示:")
                            .setMessage("文件的url地址不合法哟，无法进行下载")
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    return;
                                }
                            }).create().show();
                }
                startDownload();
            }
        }
    }

    private void findViewById() {
        tv_download = findViewById(R.id.tv_download);
        ImageView iv_back = findViewById(R.id.iv_back);
        tv_title = findViewById(R.id.tv_title);
        progressBar_download = findViewById(R.id.progressBar_download);
        rl_tbsView = findViewById(R.id.rl_tbsView);
        iv_back.setOnClickListener(this);
    }

    /**
     * 获取传过来的文件url和文件名
     */
    private void getFileUrlByIntent() {
        Intent intent = getIntent();
        mFileUrl = intent.getStringExtra("fileUrl");
        fileName = intent.getStringExtra("fileName");
        tv_title.setText("PDF文件预览");
    }

    /**
     * 跳转页面
     *
     * @param context
     * @param fileUrl  文件url
     * @param fileName 文件名
     */
    public static void actionStart(Context context, String fileUrl, String fileName) {
        Intent intent = new Intent(context, FileDisplayActivity.class);
        intent.putExtra("fileUrl", fileUrl);
        intent.putExtra("fileName", fileName);
        context.startActivity(intent);
    }

    /**
     * 加载显示文件内容
     *
     * @param
     */
    private void displayFile() {
        Bundle bundle = new Bundle();
        if (isLocalExist()) {
            bundle.putString("filePath", getLocalFile().getPath());
        }
        if (mFileUrl.contains("storage")) {
            bundle.putString("filePath", mFileUrl);
        }
        bundle.putString("tempPath", Environment.getExternalStorageDirectory().getPath());
        boolean result = mTbsReaderView.preOpen(PDFUtil.parseFormat(mFileName), false);
        if (result) {//如果腾讯Tbs插件内核加载成功
            mTbsReaderView.openFile(bundle);
        } else {//如果腾讯tbs插件加载失败走系统内核
            File file = new File(getLocalFile().getPath());
            if (file.exists()) {
                Intent openintent = new Intent();
                openintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String type = PDFUtil.getMIMEType(file);
                // 设置intent的data和Type属性。
                openintent.setDataAndType(Uri.fromFile(file), type);
                // 跳转
                startActivity(openintent);
                finish();
            }
        }
    }


    private boolean isLocalExist() {
        return getLocalFile().exists();
    }

    private File getLocalFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mFileName);
    }

    /**
     * 下载文件
     */
    @SuppressLint("NewApi")
    private void startDownload() {
        mDownloadObserver = new DownloadObserver(new Handler());
        getContentResolver().registerContentObserver(
                Uri.parse("content://downloads/my_downloads"), true,
                mDownloadObserver);

        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //将含有中文的url进行encode
        String fileUrl = PDFUtil.toUtf8String(mFileUrl);
        try {

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(fileUrl));
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, mFileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            mRequestId = mDownloadManager.enqueue(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(mRequestId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                // 已经下载的字节数
                long currentBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                // 总需下载的字节数
                long totalBytes = cursor
                        .getLong(cursor
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                // 状态所在的列索引
                int status = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
                tv_download.setText("下载中...(" + PDFUtil.FormetFileSize(currentBytes)
                        + "/" + PDFUtil.FormetFileSize(totalBytes) + ")");
                // 将当前下载的字节数转化为进度位置
                int progress = (int) ((currentBytes * 1.0) / totalBytes * 100);
                progressBar_download.setProgress(progress);

                Log.i("downloadUpdate: ", currentBytes + " " + totalBytes + " "
                        + status + " " + progress);
                if (DownloadManager.STATUS_SUCCESSFUL == status
                        && tv_download.getVisibility() == View.VISIBLE) {
                    tv_download.setVisibility(View.GONE);
                    tv_download.performClick();
                    if (isLocalExist()) {
                        tv_download.setVisibility(View.GONE);
                        displayFile();
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTbsReaderView.onStop();
        if (mDownloadObserver != null) {
            getContentResolver().unregisterContentObserver(mDownloadObserver);
        }
    }

    @SuppressLint("Override")
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class DownloadObserver extends ContentObserver {

        private DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            queryDownloadStatus();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }


}
